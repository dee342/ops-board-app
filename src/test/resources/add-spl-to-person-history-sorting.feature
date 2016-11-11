Feature: Spl Position

Scenario: Add Spl to Person History Sorting

Given sanitation worker "10388"
When add spl to sanitation worker
And edit history record for this assignment
Then second assignment is at the top of spl history 

