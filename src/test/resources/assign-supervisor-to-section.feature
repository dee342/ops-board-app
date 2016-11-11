Feature: Assign Supervisor

Scenario: Assign a supervisor to Supervision task

Given supervisor "10716"
And supervisionTask "5ccc24d2-deae-4c80-a546-17b88b5d11fd"
And boardDate "20150309"
And location "BKN03"
And homeLocation "BKN03"
When supervisor assigned to supervisionTask
Then supervisionTask has supervisor assigned "10716"
Then supervisor state is "Assigned"

