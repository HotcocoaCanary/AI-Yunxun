# 输出格式详细说明

## 概述

mcp-echarts 支持多种输出格式，通过 `outputType` 参数控制：

- `"option"`：返回 ECharts 配置 JSON 字符串（**当前版本仅支持此格式**）
- `"png"`：返回 PNG 图片（**后续版本实现**）

**重要说明**：
- **当前版本仅实现 `outputType = "option"` 格式**
- `outputType = "png"` 功能将在后续版本中实现
- `width` 和 `height` 参数仅在 `outputType = "png"` 时使用，用于控制渲染图片的尺寸

## MCP 标准输出格式

所有工具都遵循 MCP (Model Context Protocol) 标准的 content 格式：

```typescript
{
  content: Array<{
    type: "text" | "image",
    text?: string,      // type="text" 时存在
    data?: string,      // type="image" 时存在（Base64 编码）
    mimeType?: string   // type="image" 时存在
  }>
}
```

## outputType = "option"

### 格式说明

当 `outputType` 为 `"option"` 时，返回完整的 ECharts 配置 JSON 字符串。

**响应格式：**

```json
{
  "content": [
    {
      "type": "text",
      "text": "{\"title\":{\"text\":\"Chart Title\"},\"series\":[{\"type\":\"bar\",\"data\":[1,2,3]}]}"
    }
  ]
}
```

**特点：**
- `content[0].type` 始终为 `"text"`
- `content[0].text` 包含完整的 ECharts option JSON 字符串
- JSON 字符串可以直接解析使用

### 使用方式

#### 前端使用

```javascript
// 1. 解析 JSON 字符串
const option = JSON.parse(response.content[0].text);

// 2. 初始化图表
const chart = echarts.init(document.getElementById('chart'));

// 3. 设置配置
chart.setOption(option);
```

#### Java 后端生成

```java
// 1. 构建 ECharts option 对象
ObjectNode option = buildEChartsOption(request);

// 2. 转换为 JSON 字符串
String optionJson = objectMapper.writeValueAsString(option);

// 3. 构建 MCP 响应
Map<String, Object> content = new HashMap<>();
content.put("type", "text");
content.put("text", optionJson);

Map<String, Object> response = new HashMap<>();
response.put("content", Arrays.asList(content));
```

### 优势

1. **零依赖**：不需要图片渲染引擎
2. **灵活性高**：前端可以完全控制图表显示
3. **交互性强**：前端 ECharts 实例支持完整的交互功能
4. **体积小**：JSON 字符串比图片文件小得多
5. **易于调试**：可以直接查看和修改配置

### 适用场景

- 前端可以直接使用 ECharts 的场景
- 需要图表交互功能的场景
- 需要动态更新数据的场景
- 对性能要求较高的场景（避免图片传输）

## outputType = "png"（暂未实现）

### 格式说明

当 `outputType` 为 `"png"` 时，返回渲染后的 PNG 图片。**此功能将在后续版本中实现。**

**注意**：当前版本仅支持 `outputType = "option"`。

#### 如果配置了对象存储（如 MinIO）

**响应格式：**

```json
{
  "content": [
    {
      "type": "text",
      "text": "http://localhost:9000/mcp-echarts/charts/1234567890.png"
    }
  ]
}
```

**特点：**
- `content[0].type` 为 `"text"`
- `content[0].text` 包含图片的 URL

#### 如果未配置对象存储（Base64 回退）

**响应格式：**

```json
{
  "content": [
    {
      "type": "image",
      "data": "iVBORw0KGgoAAAANSUhEUgAA...",
      "mimeType": "image/png"
    }
  ]
}
```

**特点：**
- `content[0].type` 为 `"image"`
- `content[0].data` 包含 Base64 编码的图片数据
- `content[0].mimeType` 为 `"image/png"`

### 使用方式

#### URL 方式

```html
<img src="http://localhost:9000/mcp-echarts/charts/1234567890.png" />
```

#### Base64 方式

```html
<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..." />
```

```javascript
const imageData = response.content[0].data;
const mimeType = response.content[0].mimeType;
const imageSrc = `data:${mimeType};base64,${imageData}`;

const img = document.createElement('img');
img.src = imageSrc;
document.body.appendChild(img);
```

### Java 实现

#### 方案 1：返回 Base64（推荐用于 Java）

```java
// 1. 渲染图表为 BufferedImage（需要使用 headless 渲染引擎）
BufferedImage image = renderChart(option, width, height);

// 2. 转换为 Base64
ByteArrayOutputStream baos = new ByteArrayOutputStream();
ImageIO.write(image, "png", baos);
String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

// 3. 构建 MCP 响应
Map<String, Object> content = new HashMap<>();
content.put("type", "image");
content.put("data", base64);
content.put("mimeType", "image/png");

Map<String, Object> response = new HashMap<>();
response.put("content", Arrays.asList(content));
```

#### 方案 2：返回 URL（需要对象存储）

```java
// 1. 渲染图表
BufferedImage image = renderChart(option, width, height);

// 2. 上传到对象存储
String url = uploadToStorage(image);

// 3. 构建 MCP 响应
Map<String, Object> content = new HashMap<>();
content.put("type", "text");
content.put("text", url);

Map<String, Object> response = new HashMap<>();
response.put("content", Arrays.asList(content));
```

### 渲染引擎选择（Java）

Java 中渲染 ECharts 图表需要 headless 浏览器或 JavaScript 执行环境：

1. **HtmlUnit**：纯 Java headless 浏览器
2. **Selenium WebDriver** + **ChromeDriver**：需要 Chrome/Chromium
3. **GraalVM** + **GraalJS**：在 JVM 中运行 JavaScript
4. **J2V8**：V8 引擎的 Java 绑定

**注意**：渲染图片比返回 option 复杂得多，建议优先支持 `"option"` 格式。

### 优势

1. **直接展示**：可以直接在网页中显示，无需 JavaScript
2. **兼容性好**：适用于不支持 JavaScript 的环境
3. **易于分享**：图片可以保存和分享

### 适用场景

- 需要静态图片的场景
- 不支持 JavaScript 的环境
- 需要图片文件的场景（报告、邮件等）
- 移动端应用

## 输出格式选择建议

### 对于 Java 实现

1. **当前版本仅实现 `"option"` 格式**：
   - 实现简单（只需要构建 JSON）
   - 不需要渲染引擎
   - 性能好，体积小
   - 前端可以使用完整的 ECharts 功能
   - 使用 Jackson ObjectNode 构建配置，序列化为 JSON 字符串

2. **后续版本将实现 `"png"` 格式**：
   - 需要使用 headless 浏览器或 JavaScript 执行环境
   - 可以考虑使用 HtmlUnit、Selenium 或其他渲染方案
   - 参考 [mcp-echarts](https://github.com/hustcc/mcp-echarts) 项目的实现

### 参数建议

```java
public enum OutputType {
    OPTION("option");  // 当前版本仅支持此格式
    
    // PNG 格式将在后续版本中实现
}
```

### width 和 height 参数说明

- **当 `outputType = "option"` 时**：`width` 和 `height` 参数会被忽略，不需要处理
- **当 `outputType = "png"` 时**：这两个参数用于控制渲染图片的尺寸（后续实现）

## 示例对比

### 相同的图表，不同输出格式

#### outputType = "option"

```json
{
  "content": [
    {
      "type": "text",
      "text": "{\"title\":{\"text\":\"销售数据\"},\"xAxis\":{\"type\":\"category\",\"data\":[\"一月\",\"二月\",\"三月\"]},\"yAxis\":{\"type\":\"value\"},\"series\":[{\"type\":\"bar\",\"data\":[120,200,150]}]}"
    }
  ]
}
```

**大小**：约 200 字节

#### outputType = "png" (Base64)

```json
{
  "content": [
    {
      "type": "image",
      "data": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFBQIAX8jx0gAAAABJRU5ErkJggg==",
      "mimeType": "image/png"
    }
  ]
}
```

**大小**：约 10-50 KB（取决于图片尺寸和压缩）

## 总结

对于 Java 实现的建议：

1. **当前版本必须实现**：`outputType = "option"` - 这是最简单和实用的格式
2. **后续版本实现**：`outputType = "png"` - 需要渲染引擎支持
3. **暂不支持**：`outputType = "svg"` - 除非有明确需求

实现参考：
- **逻辑参考**：[mcp-echarts](https://github.com/hustcc/mcp-echarts) - TypeScript 实现，可作为图表构建逻辑的参考
- **框架参考**：项目中的 Neo4j 工具实现方式

实现要点：
1. 使用 `@McpTool` 注解标记工具方法
2. 使用 Jackson ObjectNode 构建 ECharts option
3. 返回 MCP 标准格式的 content 数组
4. 错误处理遵循 MCP 协议规范（抛出异常）

