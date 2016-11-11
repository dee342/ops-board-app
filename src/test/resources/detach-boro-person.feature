Feature: Boro Person Detachment 

Scenario: Detach Boro Person

Given superintendent "10059"
And boro boardDate "20150310"
And boro homeLocation BXBO
And boro location BXone
When detach person to BXone
Then the state of person is Detached
And person is not assigned

