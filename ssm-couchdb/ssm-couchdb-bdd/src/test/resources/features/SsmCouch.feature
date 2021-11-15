Feature:

  Scenario: As a developper, I want to get changes
    Given An admin
    And A new user "Looper"
    And A new user "Engineer"
    And A ssm "loop-ssm" with transitions
      | from | to | role    | action |
      | 0    | 0  | Looping | Loop   |
      | 0    | 1  | Engine  | Exit   |
    And I start a session "loop-ssm-session" for "loop-ssm"
      | role    | userName |
      | Looping | Looper   |
      | Engine  | Engineer |
    And I perform actions
      | action | iteration | sessionName      | userName | public        |
      | Loop   | 0         | loop-ssm-session | Looper   | A loop        |
      | Loop   | 1         | loop-ssm-session | Looper   | An other loop |
      | Loop   | 2         | loop-ssm-session | Looper   | An other loop |
      | Exit   | 3         | loop-ssm-session | Engineer | No more loop  |
    Then Changes for session "loop-ssm-session" for "loop-ssm" is
      | docType | objectId         |
      | ssm     | loop-ssm         |
      | state   | loop-ssm-session |
    Then Changes for session "loop-ssm-session" for "loop-ssm" is empty