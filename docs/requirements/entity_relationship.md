Project 1 --- * Issue (0~*)
Project 1 --- * Tag (0~*)

Issue 1 --- * Comment (0~*)
Issue 1 --- * IssueHistory (1~*)
IssueHistory 1 --- 1 IssueDelta
Issue * --- * Tag  (0~* , 0~*) 

Account 1 --- * Issue   (as reporter) ← Tester
Account 1 --- * Issue   (as assignee) ← Dev
Account 1 --- * Issue   (as fixer) ← Dev
Account 1 --- * Comment (0~*)
Account 1 --- * IssueHistory
