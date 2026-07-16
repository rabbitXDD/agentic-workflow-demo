---
name: ChatOps 摘要小幫手

# 使用官方支援的 issue_comment 觸發
on:
  issue_comment:
    types: [created]

# 安全規範：AI 本體只能唯讀，寫入交給下方宣告的 safe-outputs
permissions: 
  contents: read
  issues: read
  pull-requests: read

# 宣告允許 AI 呼叫的安全輸出工具
safe-outputs:
  add-comment:

engine: 
  id: copilot
  model: gpt-4o
tools:
  github:
---

# ChatOps 摘要小幫手

## 核心檢查 (重要)
1. 請先檢查觸發本次事件的最新一則留言 (Comment) 的內文。
2. 如果內文**沒有包含** `/summarize` 指令，你**必須**直接呼叫 `noop` 工具並附上說明：
   `{"noop": {"message": "未檢測到 /summarize 指令，無需執行任何操作。"}}`
   並且**立即終止**，不要執行下方的摘要任務。

## 摘要任務步驟
只有當確認最新留言中**明確包含** `/summarize` 時，才執行以下步驟：

1. **閱讀討論串**：使用 `github` 工具閱讀此 Issue（或 Pull Request）從建立至今的完整討論串。
2. **生成摘要內容**：將收集到的討論，嚴謹地整理出以下三段重點：
   * **目前共識**：
   * **待決問題**：
   * **下一步行動**：
3. **語系限制**：摘要內容必須完全以**正體中文 (Traditional Chinese)** 呈現，語氣保持專業、客觀。
4. **輸出回覆**：呼叫 `add-comment` 工具，將最終整理好的正體中文摘要發布到當前的討論串中。
