Feature: Person Unavailable 

Scenario: Add Una to Person

Given person "10388"
And set boardDate "20150310"
And set location "BKS13"
And set homeLocation "BKS13"
When add vacation unavailable to sanitation worker
And change then delete unavailable code
Then the state of person is "Available"

