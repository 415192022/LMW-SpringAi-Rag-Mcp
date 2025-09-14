package com.itzixi.config;

import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import lombok.SneakyThrows;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Configuration
public class EmbeddingModelConfig {

    @Value("${spring.ai.transformers.embedding.model-path:classpath:models/model.onnx}")
    private String modelPath;

    private final ResourceLoader resourceLoader;

    public EmbeddingModelConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public EmbeddingModel embeddingModel() throws IOException, OrtException {
        Resource modelResource = resourceLoader.getResource(modelPath);
        return new SimpleOnnxEmbeddingModel(modelResource.getFile().getAbsolutePath());
    }

    /**
     * 简单的ONNX模型包装器，直接使用ONNX Runtime
     */
    private static class SimpleOnnxEmbeddingModel implements EmbeddingModel {

        private final String modelPath;
        private OrtEnvironment env;
        private OrtSession session;

        public SimpleOnnxEmbeddingModel(String modelPath) throws OrtException {
            this.modelPath = modelPath;
            env = OrtEnvironment.getEnvironment();
            session = env.createSession(modelPath, new OrtSession.SessionOptions());
        }

        @SneakyThrows
        @Override
        public float[] embed(String text) {
            if (text == null || text.trim().isEmpty()) {
                // 对于空文本，返回全零向量
                return new float[dimensions()];
            }
            
            // 文本预处理：去除多余空格，转小写
            text = text.trim().toLowerCase();
            
            // 创建模型输入
            Map<String, OnnxTensor> inputs = new HashMap<>();
            OnnxTensor inputTensor = null;
            OnnxTensor tokenTypeIdsTensor = null;
            OnnxTensor attentionTensor = null;
            OrtSession.Result result = null;
            
            try {
                // 获取模型的输入节点信息
                Map<String, NodeInfo> inputInfo = session.getInputInfo();
                System.out.println("模型输入节点: " + inputInfo.keySet());
                
                // BERT模型需要特定的分词方式，这里使用简化的方法
                // 实际应用中应该使用专门的BERT分词器
                
                // 根据错误信息，模型的词汇表范围是[-30522,30521]
                // 使用以下固定的token IDs
                // 101: [CLS] token (句子开始)
                // 102: [SEP] token (句子结束)
                // 其他: 使用一些安全的token ID范围
                
                char[] chars = text.toCharArray();
                int maxLength = 128; // 限制最大长度
                int actualLength = Math.min(chars.length, maxLength - 2); // 减去[CLS]和[SEP]
                
                // 创建token IDs: [CLS] + text + [SEP]
                long[] tokenIds = new long[actualLength + 2];
                tokenIds[0] = 101; // [CLS] token
                
                // 使用一些安全的token ID
                // BERT基本词汇表范围通常是0-30000
                // 为简单起见，使用一些常用token ID
                for (int i = 0; i < actualLength; i++) {
                    char c = chars[i];
                    // 使用一些基本的映射，确保在词汇表范围内
                    if (c >= 'a' && c <= 'z') {
                        tokenIds[i + 1] = 1000 + (c - 'a'); // 使用1000-1025范围的ID
                    } else if (c >= 'A' && c <= 'Z') {
                        tokenIds[i + 1] = 1000 + (c - 'A'); // 使用相同范围，不区分大小写
                    } else if (c >= '0' && c <= '9') {
                        tokenIds[i + 1] = 2000 + (c - '0'); // 使用2000-2009范围的ID
                    } else {
                        tokenIds[i + 1] = 999; // 对于其他字符使用通用ID
                    }
                }
                
                tokenIds[actualLength + 1] = 102; // [SEP] token
                
                int sequenceLength = actualLength + 2; // 实际序列长度
                
                try {
                    // 创建输入形状
                    long[] inputShape = new long[]{1, sequenceLength}; // 批次大小为1
                    
                    // 创建input_ids张量
                    java.nio.LongBuffer longBuffer = java.nio.LongBuffer.wrap(tokenIds);
                    inputTensor = OnnxTensor.createTensor(env, longBuffer, inputShape);
                    inputs.put("input_ids", inputTensor);
                    
                    // 创建token_type_ids张量 - 全0
                    long[] tokenTypeIds = new long[sequenceLength];
                    // 默认全部为0，表示单个句子
                    java.nio.LongBuffer tokenTypeBuffer = java.nio.LongBuffer.wrap(tokenTypeIds);
                    tokenTypeIdsTensor = OnnxTensor.createTensor(env, tokenTypeBuffer, inputShape);
                    inputs.put("token_type_ids", tokenTypeIdsTensor);
                    
                    // 创建attention_mask张量 - 全1
                    long[] attentionMask = new long[sequenceLength];
                    for (int i = 0; i < sequenceLength; i++) {
                        attentionMask[i] = 1L; // 1表示关注这个token，0表示忽略
                    }
                    java.nio.LongBuffer attentionBuffer = java.nio.LongBuffer.wrap(attentionMask);
                    attentionTensor = OnnxTensor.createTensor(env, attentionBuffer, inputShape);
                    inputs.put("attention_mask", attentionTensor);
                    
                    // 打印前10个token ID用于调试
                    StringBuilder tokenDebug = new StringBuilder("Token IDs前10个: ");
                    for (int i = 0; i < Math.min(10, sequenceLength); i++) {
                        tokenDebug.append(tokenIds[i]).append(" ");
                    }
                    System.out.println(tokenDebug.toString());
                    System.out.println("使用BERT格式输入，序列长度: " + sequenceLength + "，包含[CLS]和[SEP]标记，已添加token_type_ids和attention_mask");
                } catch (Exception e) {
                    throw new RuntimeException("创建输入张量失败: " + e.getMessage(), e);
                }
                
                // 运行推理
                result = session.run(inputs);
                
                // 获取输出 - 尝试获取第一个输出
                float[] embeddingVector = null;
                
                // 尝试不同的输出格式
                try {
                    // 首先尝试获取三维输出 (批次, 序列长度, 隐藏维度)
                    // 这是BERT模型常见的输出格式
                    try {
                        float[][][] embeddings3D = (float[][][]) result.get(0).getValue();
                        System.out.println("检测到三维输出，形状: [" + embeddings3D.length + ", " + 
                                         embeddings3D[0].length + ", " + embeddings3D[0][0].length + "]");
                        // 通常我们取第一个批次，第一个token（[CLS]标记）的表示作为句子嵌入
                        embeddingVector = embeddings3D[0][0];
                    } catch (Exception e3D) {
                        // 尝试获取二维输出 (批次, 维度)
                        try {
                            float[][] embeddings2D = (float[][]) result.get(0).getValue();
                            System.out.println("检测到二维输出，形状: [" + embeddings2D.length + ", " + 
                                             embeddings2D[0].length + "]");
                            embeddingVector = embeddings2D[0];
                        } catch (Exception e2D) {
                            // 尝试获取一维输出
                            embeddingVector = (float[]) result.get(0).getValue();
                            System.out.println("检测到一维输出，长度: " + embeddingVector.length);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("无法解析模型输出: " + e.getMessage(), e);
                }
                
                // 验证向量维度
                if (embeddingVector.length != dimensions()) {
                    System.out.println("警告：生成的向量维度 " + embeddingVector.length + 
                                     " 与预期维度 " + dimensions() + " 不匹配");
                }
                
                // 返回嵌入向量
                return embeddingVector;
            } catch (OrtException e) {
                throw new RuntimeException("ONNX模型推理失败: " + e.getMessage(), e);
            } finally {
                // 确保资源被释放
                if (inputTensor != null) {
                    try {
                        inputTensor.close();
                    } catch (Exception e) {
                        // 忽略关闭异常
                    }
                }
                if (tokenTypeIdsTensor != null) {
                    try {
                        tokenTypeIdsTensor.close();
                    } catch (Exception e) {
                        // 忽略关闭异常
                    }
                }
                if (attentionTensor != null) {
                    try {
                        attentionTensor.close();
                    } catch (Exception e) {
                        // 忽略关闭异常
                    }
                }
                if (result != null) {
                    try {
                        result.close();
                    } catch (Exception e) {
                        // 忽略关闭异常
                    }
                }
            }
        }

        @Override
        public float[] embed(Document document) {
            return embed(document.getText());
        }

        @Override
        public List<float[]> embed(List<String> texts) {
            // 批量嵌入
            return texts.stream()
                    .map(this::embed)
                    .toList();
        }

        @Override
        public EmbeddingResponse embedForResponse(List<String> texts) {
            List<float[]> embeddings = embed(texts);

            // 创建EmbeddingResponse，为每个嵌入向量分配索引
            List<org.springframework.ai.embedding.Embedding> embeddingList = embeddings.stream()
                    .map(embedding -> new org.springframework.ai.embedding.Embedding(embedding, 0))
                    .toList();

            return new EmbeddingResponse(embeddingList);
        }

        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            return embedForResponse(request.getInstructions());
        }

        @Override
        public int dimensions() {
            return 384; // 根据你的模型调整维度
        }
    }
}
