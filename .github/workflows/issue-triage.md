---
on:
  issues: 
    types: [opened,reopened]

permissions: 
  contents: read
  issues: read

safe-outputs:
  add-comment:
  add-labels:
    allowed: [bug, enhancement, question, documentation, "priority:high", "priority:medium", "priority:low"]

engine: 
  id: copilot
  model: gpt-4o
tools:
  github:
---

# Issue 自動分類
請分析這個 issue 並：
1. 依內容判斷類型，從允許清單中貼上合適的標籤（type與priority各一）。
2.搜尋既有 issue ，若疑似重複，於留言中以連結指出可能重複的對象。
3.
若描述不清楚，友善地詢問需要補充的資訊。
保持精簡、附上相關 issue 連結，並以正體中文回覆。