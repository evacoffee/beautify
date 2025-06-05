# BeautyMod - Dating System Commands

This document outlines all available commands for the dating system in BeautyMod.

## Date Planning Commands

### `/date plan <type> <target>`
Plan a date with another player or NPC.
- `type`: The type of date (e.g., "romantic", "casual", "active")
- `target`: The player or NPC to go on a date with

### `/date locations [filter]`
List available date locations.
- `filter`: Optional filter (e.g., "romantic", "outdoor", "indoor")

### `/date invite <player>`
Invite a player to a planned date.

### `/date accept <player>`
Accept a date invitation.

### `/date decline <player>`
Decline a date invitation.

### `/date cancel [reason]`
Cancel a planned date.
- `reason`: Optional reason for cancellation

## Date Management Commands

### `/date status [player]`
Check the status of current or upcoming dates.
- `player`: Optional player name to check their date status

### `/date history [player]`
View your date history or another player's date history.
- `player`: Optional player name to view their history

### `/date feedback <player> <rating> [comment]`
Provide feedback after a date.
- `player`: The player you went on a date with
- `rating`: Rating from 1-5 stars
- `comment`: Optional comment about the date

## Gift Commands

### `/gift give <player> <item>`
Give a gift to another player or NPC.
- `player`: The recipient of the gift
- `item`: The item to gift

### `/gift wrap <item>`
Wrap an item to create a gift.
- `item`: The item to wrap

### `/gift preferences`
View your gift preferences for others to see.

## Location Commands

### `/location setdate <name> <type>`
Mark your current location as a date spot.
- `name`: Name for this location
- `type`: Type of date location (e.g., "romantic", "casual")

### `/location list [type]`
List your saved date locations.
- `type`: Optional filter by location type

### `/location remove <name>`
Remove a saved date location.
- `name`: Name of the location to remove

## Relationship Commands

### `/relationship status [player]`
Check your relationship status with another player or NPC.
- `player`: Optional player/NPC name

### `/relationship stats`
View your relationship statistics.

### `/relationship setstatus <player> <status>`
Set your relationship status with another player.
- `player`: The other player
- `status`: The status (e.g., "friends", "dating", "married")

## Admin Commands

### `/datingadmin reload`
Reload dating system configuration.

### `/datingadmin setdate <player1> <player2> <type>`
Admin command to set up a date between two players.
- `player1`: First participant
- `player2`: Second participant
- `type`: Type of date

### `/datingadmin addlocation <name> <type> [x] [y] [z]`
Admin command to add a date location.
- `name`: Name of the location
- `type`: Type of location
- `x, y, z`: Optional coordinates (defaults to current position)

## Help Commands

### `/dating help [command]`
Show help for dating commands.
- `command`: Optional specific command to get help for

### `/dating tutorial`
Start the dating system tutorial.

## Tips
- Use tab completion to see available options for each command
- Most commands can be used with NPCs by using their name
- Some commands may require certain relationship levels
- Check your date history for special date ideas and achievements
