Feature:

  Scenario: As a developer, I want to get all ssm in a database
    Given An admin
    And A ssm "db-entries-ssm" with transitions
      | from | to | role   | action |
      | 0    | 1  | Tester | Test   |
    Given I have a local database
    When I get all ssm for
      | channelId        | chaincodeId  |
      | sandbox          | ssm          |
    Then I found ssm