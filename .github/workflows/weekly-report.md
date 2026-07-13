---
on:
  schedule: weekly

permissions: 
  contents: read
  issues: read
  pull-requests: read

safe-outputs:
  create-issue:
    title-prefix: "[weekly] "
    labels: [report]

engine: 
  id: copilot
  model: gpt-4o
tools:
  github:
---

# 每週 Repo 健康週報
請每週彙整並開一個 issue ，內容包含： 
1. 本週新增／關閉的 issue 與合併的 PR 概況。
2. 仍卡關（ open blockers ）的項目與停滯過久的 PR。
3. 值得關注的趨勢與給維護者的下一步建議。
條列清楚、附上連結，並以正體中文撰寫

