Feature: Detach Equipment

Scenario: detach an attached vehicle

Given equipment "25DC-001"
And equipment owner "BKN01"
When detach equipment from owner to "BKN02"
Then detachment is added to top of equipment detachment history
And equipment state at owner is "Pending Detach"
And equipment state at to is "Pending Attach"
