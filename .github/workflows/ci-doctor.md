---
on:
  workflow_run:
    workflows: ["CI"]
    types: [completed]

permissions: 
  contents: read
  actions: read

safe-outputs:
  create-issue:
    title-prefix: "[ci-doctor]"
    labels: [ci-failure]

engine: 
  id: copilot
  model: gpt-4o
tools:
  github:
---

# # CI 失敗診斷醫生
當被監看的 CI 失敗時，請：
1.
取得失敗的工作與步驟紀錄，找出最可能的根本原因。
2.
開一個issue
，清楚說明：失敗的測試／步驟、推測原因、建議的修正方向。
3.
附上相關
commit
或檔案連結，方便維護者快速定位。
保持精簡、可執行，並以正體中文撰寫

