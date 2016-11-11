Feature: Person Detachment 

Scenario: Detach Person from to Home to Another and back within the same time period

Given a person with id '10059' at homeLocation 'BX01'
When that person is detached to 'BX02' from '20150325' to '20150328'
And then detached back to 'BX01' from '20150326' to '20150326'
Then the state of person for 'BX01' with board date '20150324' should be 'Available'
Then the state of person for 'BX01' with board date '20150325' should be 'Detached'
Then the state of person for 'BX01' with board date '20150326' should be 'Available'
Then the state of person for 'BX01' with board date '20150327' should be 'Detached'
Then the state of person for 'BX01' with board date '20150328' should be 'Detached'
Then the state of person for 'BX01' with board date '20150329' should be 'Available'
Then the state of person for 'BX02' with board date '20150324' should be 'Hidden'
Then the state of person for 'BX02' with board date '20150325' should be 'Available'
Then the state of person for 'BX02' with board date '20150326' should be 'Hidden'
Then the state of person for 'BX02' with board date '20150327' should be 'Available'
Then the state of person for 'BX02' with board date '20150328' should be 'Available'
Then the state of person for 'BX02' with board date '20150329' should be 'Hidden'