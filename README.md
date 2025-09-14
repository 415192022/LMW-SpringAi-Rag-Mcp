# Spring AI MCP Client - 本地模型配置指南

## 项目概述
这是一个基于Spring AI框架开发的智能聊天应用，集成了检索增强生成（RAG）和模型上下文协议（MCP）技术，提供了智能对话、文档检索和互联网搜索等功能。项目支持使用本地ONNX模型进行文本嵌入，无需依赖外部API。

### 核心功能

- **智能聊天**：支持用户与AI助手进行基础文本对话，包括流式响应和对话历史记忆
- **检索增强生成（RAG）**：通过向量检索相关文档，增强AI生成的回复质量
- **互联网搜索**：通过SearXng搜索引擎进行互联网搜索，提供实时信息
- **模型上下文协议（MCP）**：扩展AI模型的能力，支持文件系统访问和工具回调
- **服务器发送事件（SSE）**：实现服务器到客户端的实时通信

## 本地模型配置问题 ✅
- **问题描述**: 项目在启动时出现 "Failed to load Huggingface native library" 错误

## 解决方案
1. **排除自动配置**: 在 `Application.java` 中排除了 `TransformersEmbeddingModelAutoConfiguration`
2. **自定义配置类**: 创建了 `EmbeddingModelConfig` 类，提供自定义的 `EmbeddingModel` 实现
3. **避免依赖冲突**: 不再使用 `TransformersEmbeddingModel`，避免了Hugging Face tokenizer的依赖问题

## 本地模型配置

### 1. 模型文件位置
将你的ONNX模型文件放在以下位置：
```
src/main/resources/models/model.onnx
```

### 2. 配置文件设置
在 `application.yml` 中添加以下配置：

```yaml
spring:
  ai:
    transformers:
      embedding:
        model-path: classpath:models/model.onnx
        model-name: local-embedding-model
```

### 3. 依赖配置
确保 `pom.xml` 中包含以下依赖：

```xml
<!-- Spring AI Transformers 支持 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-transformers</artifactId>
</dependency>

<!-- ONNX Runtime 支持 -->
<dependency>
    <groupId>com.microsoft.onnxruntime</groupId>
    <artifactId>onnxruntime</artifactId>
    <version>1.18.0</version>
</dependency>
```

### 4. 自动配置排除
在 `Application.java` 中排除自动配置：

```java
@SpringBootApplication(exclude = {
    org.springframework.ai.model.transformers.autoconfigure.TransformersEmbeddingModelAutoConfiguration.class
})
```

### 5. 自定义配置类
项目使用 `EmbeddingModelConfig` 类来手动配置本地模型。

## 当前状态
- ✅ 项目编译成功
- ✅ 应用启动成功
- ✅ 监听端口9090
- ✅ 不再出现Hugging Face库加载错误

## 模型参数调整
根据你的具体模型，可能需要调整以下参数：

- `embeddingDimensions`: 嵌入向量维度（当前设置为5）
- `maxSequenceLength`: 最大序列长度

## 注意事项
1. 确保ONNX模型文件格式正确
2. 模型文件较大，建议使用Git LFS管理
3. 首次加载模型可能需要较长时间
4. 确保有足够的内存来加载模型

## 故障排除
如果遇到问题：
1. 检查模型文件路径是否正确
2. 确认ONNX Runtime依赖已添加
3. 验证模型文件是否损坏
4. 检查内存是否充足

## 文本转向量模型实现

### 技术实现方式

```

## 项目运行时逻辑与流程图

### 运行时逻辑分析

通过对项目代码的分析，可以梳理出系统的运行时逻辑如下：

#### 客户端流程

1. 用户通过前端界面发送消息到客户端的ChatController
2. ChatController调用ChatService处理用户请求
3. ChatService使用Spring AI的ChatClient构建提示词并发送给AI模型
4. AI模型生成回答，可能会调用工具（Tool）来完成特定任务
5. 如果需要RAG检索，系统会从向量数据库中检索相关文档
6. 如果需要互联网搜索，系统会调用SearXngService进行搜索
7. 最终结果通过SSE（Server-Sent Events）实时返回给用户

#### 服务端流程

1. 服务端启动时，通过Application类注册所有工具（DateTool、EmailTool、ProductTool、SenderTool）
2. 当AI模型需要调用工具时，请求发送到服务端
3. 服务端根据工具名称和参数执行相应的方法
4. 工具执行结果返回给AI模型
5. AI模型根据工具执行结果继续生成回答

### 系统流程图

```
+--------+     请求     +-------------+     调用     +------------+
|        | ----------> |             | ----------> |            |
|  用户  |             | 聊天控制器   |             |  聊天服务   |
|        | <---------- |             | <---------- |            |
+--------+     响应     +-------------+     结果     +------------+
                                                        |  ^
                                                        |  |
                                           构建提示词   |  | 返回结果
                                                        |  |
                                                        v  |
+--------+     需要     +-------------+     请求     +------------+
|        | <---------- |             | <---------- |            |
| AI模型 |             | 工具调用处理 |             |  AI客户端   |
|        | ----------> |             | ----------> |            |
+--------+    结果     +-------------+     结果     +------------+
    ^                        |                          ^
    |                        |                          |
    |                        |                          |
    |                        v                          |
    |               +-------------+                     |
    |               |             |                     |
    +---------------|  各种工具   |---------------------+
         返回结果   |             |      调用工具
                    +-------------+
```

### 系统流程说明

#### 基本流程

1. **用户请求处理**
   - 用户发送请求到聊天控制器
   - 聊天控制器将请求转发给聊天服务

2. **AI交互流程**
   - 聊天服务构建提示词发送给AI客户端
   - AI客户端将请求发送给AI模型
   - AI模型生成回复返回给AI客户端

3. **工具调用流程**
   - 当AI模型需要使用工具时，发送工具调用请求
   - 工具调用处理器接收请求并找到对应工具
   - 工具执行操作并返回结果
   - 结果返回给AI模型继续生成回复

4. **响应返回流程**
   - AI客户端将最终结果返回给聊天服务
   - 聊天服务处理结果并返回给聊天控制器
   - 聊天控制器将响应返回给用户

#### 核心组件功能

- **聊天控制器**：处理HTTP请求和响应，负责与用户交互
- **聊天服务**：核心业务逻辑，协调整个对话流程
- **AI客户端**：与AI模型交互，发送提示词和接收回复
- **工具调用处理**：管理和执行各种工具调用
- **各种工具**：提供特定功能的工具集合（日期、邮件、产品查询等）

#### 特殊流程

- **RAG检索**：当需要基于知识库回答问题时，系统会将用户问题转换为向量，在向量数据库中检索相似文档，并将检索结果作为上下文提供给AI模型
- **互联网搜索**：当需要最新信息时，系统会调用搜索服务执行互联网搜索，并将搜索结果作为上下文提供给AI模型

### 详细流程说明

#### 1. 用户请求处理流程

1. 用户发送消息到`/chat/doChat`接口
2. `ChatController`接收请求并调用`ChatService.doChat()`方法
3. `ChatService`使用`ChatClient`处理用户消息
4. `ChatClient`将用户消息转换为提示词（Prompt）并发送给AI模型
5. 系统使用`Flux`和SSE实现流式响应，实时返回AI生成的内容

#### 2. 工具调用流程

1. AI模型在生成回答过程中，可能需要调用工具来完成特定任务
2. AI模型通过MCP（Model Completion Protocol）协议发送工具调用请求
3. 请求由`ToolCallbackProvider`接收并路由到相应的工具类
4. 工具类（如`SenderTool`、`EmailTool`等）执行相应的方法
5. 执行结果返回给AI模型
6. AI模型根据工具执行结果继续生成回答

#### 3. RAG检索流程

1. 当需要基于知识库回答问题时，系统会启动RAG流程
2. 系统将用户问题转换为向量表示
3. 在向量数据库中检索相似的文档
4. 将检索到的文档作为上下文添加到提示词中
5. AI模型基于上下文生成更准确的回答

#### 4. 互联网搜索流程

1. 当AI模型需要最新信息时，可能会触发互联网搜索
2. 系统调用`SearXngService`进行搜索
3. 搜索结果作为上下文添加到提示词中
4. AI模型基于搜索结果生成更全面的回答

### 核心组件交互

- **ChatClient**: 负责与AI模型交互，发送提示词并接收回答
- **ToolCallbackProvider**: 负责处理工具调用请求，路由到相应的工具类
- **ChatMemory**: 负责管理对话历史，实现上下文感知的对话
- **SSEServer**: 负责实现服务器推送，实时返回AI生成的内容

## 文本转向量模型实现

### 技术实现方式

项目使用了本地部署的ONNX格式模型，而非远程API调用：

- **模型格式**：ONNX（Open Neural Network Exchange）格式，这是一种开放的神经网络交换格式
- **运行时环境**：Microsoft ONNX Runtime
- **自定义实现**：通过`EmbeddingModelConfig`类和`SimpleOnnxEmbeddingModel`内部类实现Spring AI的`EmbeddingModel`接口

### 文本转向量的处理流程

1. **文本预处理**：
   - 去除多余空格，转小写
   - 添加[CLS]和[SEP]标记
   - 将字符映射到token ID范围内
   - 创建attention mask和token type IDs

2. **模型推理**：
   - 创建ONNX模型输入张量
   - 运行模型推理
   - 处理模型输出，支持多种输出格式
   - 返回固定维度（384）的嵌入向量

3. **向量存储**：
   - 使用Redis向量数据库存储文档向量
   - 配置自定义索引名称（lmw-vectorstore）

### 技术优势

- **隐私保护**：文本数据不需要发送到外部API
- **降低成本**：无需支付API调用费用
- **降低延迟**：避免网络传输延迟
- **离线工作**：不依赖网络连接

## 下一步
当前已实现ONNX模型的集成，可以考虑以下优化：
1. 优化分词处理，使用专业的BERT分词器
2. 根据实际应用场景调整向量维度
3. 增加更多文档类型的支持
4. 优化向量检索的性能
