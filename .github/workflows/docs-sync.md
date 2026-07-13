---
on:
  schedule: daily

permissions: 
  contents: read

safe-outputs:
  create-pull-request:
    title-prefix: "[docs] "
    labels: [documentation]

engine: 
  id: copilot
  model: gpt-4o
tools:
  github:
---

# 文件同步小幫手
請每天執行：
1. 比對近期合併的程式碼變更與現有文件（README、docs/）。
2. 找出已過時或不一致的說明。
3.開一個 Pull Request，提出必要的文件更新，並在描述中說明改了什麼、為什麼。
不要更動程式碼，只更新文件。以正體中文撰寫文件與PR說明

