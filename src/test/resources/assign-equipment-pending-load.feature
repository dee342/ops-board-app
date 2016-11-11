Feature: Assign Equipment

Scenario: Assign Equipment Pending Load

Given set equipment "1018191"
And soonEndingTask "9ba4628e-f13f-4c86-8ba5-cad27138c588"
And main location "BKN03"
When update equipment pending load status
And  equipment assigned to soonEndingTask
And equipment back to pending load
Then equipment state is pending load
And equipment has unchanged load status

