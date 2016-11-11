Feature: Supervision Assignment

Scenario: Supervision Assignment Visual Indicator

Given regular supervisor
And subcategory "606585a3-f854-4d8a-a516-0dc54b19dfdc" under supervisor category
And task "6c0b20d2-e706-483f-8841-aa7c9d499305" under subcategory
And supervisionTask "89d7e19b-1371-41d4-9009-7f017cf7b7e3"
And give boardDate "20150311"
And give homeLocation "BKS13	"
When supervisor assigned to task
And supervisor assigned to superTask
Then task has supervisor assigned "12450"
And supervisionTask has visual indicator "D/S"

