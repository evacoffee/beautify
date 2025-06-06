# BeautyMod - Commands Reference

This document outlines all available commands in the BeautyMod, including dating, marriage, character interaction, and security systems.

## Table of Contents
- [Dating System](#dating-system)
- [Marriage System](#marriage-system)
- [Character Interaction](#character-interaction)
- [Relationship Management](#relationship-management)
- [Gift System](#gift-system)
- [Location Management](#location-management)
- [Security & Privacy](#security--privacy)
- [Memory System](#memory-system)
- [Admin Commands](#admin-commands)
- [Help & Tutorials](#help--tutorials)

## Dating System

### `/date plan <type> <target>`
Plan a date with another player or NPC.
- `type`: Type of date ("romantic", "casual", "active")
- `target`: Player or NPC name

### `/date locations [filter]`
List available date locations.
- `filter`: Optional ("romantic", "outdoor", "indoor")

### `/date invite <player>`
Invite a player to a planned date.

### `/date accept <player>`
Accept a date invitation.

### `/date decline <player>`
Decline a date invitation.

### `/date cancel [reason]`
Cancel a planned date.
- `reason`: Optional cancellation reason

### `/date status [player]`
Check date status.
- `player`: Optional player name

## Marriage System

### `/marriage propose <player>`
Propose marriage to a player.
- `player`: Player to propose to

### `/marriage accept <player>`
Accept a marriage proposal.
- `player`: Player who proposed

### `/marriage divorce`
End your current marriage.

### `/marriage sethome`
Set your current location as your marital home.

### `/marriage home`
Teleport to your marital home.

### `/marriage status`
View your marriage status and partner info.

### `/marriage perks`
List available marriage perks.

## Character Interaction

### `/talk <npc>`
Start a conversation with an NPC.
- `npc`: Target NPC's name or ID

### `/testdialogue`
(Admin) Test dialogue system.

### `/lovestatus`
Check your current love level.

### `/gift <player> <item>`
Give a gift to a player or NPC.
- `player`: Recipient's name
- `item`: Item to gift

## Relationship Management

### `/relationship status [player]`
Check relationship status.
- `player`: Optional player/NPC name

### `/relationship stats`
View relationship statistics.

### `/relationship setstatus <player> <status>`
Set relationship status.
- `player`: Target player
- `status`: Status ("friends", "dating", "married")

## Gift System

### `/gift give <player> <item>`
Give a gift.
- `player`: Recipient
- `item`: Item to gift

### `/gift wrap <item>`
Wrap an item as a gift.
- `item`: Item to wrap

### `/gift preferences`
View your gift preferences.

## Location Management

### `/location setdate <name> <type>`
Mark location as a date spot.
- `name`: Location name
- `type`: Location type

### `/location list [type]`
List saved locations.
- `type`: Optional filter

### `/location remove <name>`
Remove a location.
- `name`: Location name

## Admin Commands

### `/datingadmin reload`
Reload configuration.

### `/datingadmin setdate <player1> <player2> <type>`
Force create a date.
- `player1`: First participant
- `player2`: Second participant
- `type`: Date type

### `/datingadmin addlocation <name> <type> [x] [y] [z]`
Add a date location.
- `name`: Location name
- `type`: Location type
- `x, y, z`: Coordinates (optional)

## Security & Privacy

### `/block <player>`
Block a player from interacting with you.
- `player`: Player to block

### `/unblock <player>`
Unblock a previously blocked player.
- `player`: Player to unblock

### `/permission <player> <permission> <grant|revoke>`
Manage player permissions.
- `player`: Target player
- `permission`: Permission node
- `grant|revoke`: Action to perform

## Memory System

### `/memories list`
List all villagers you've interacted with.

### `/memories <villager>`
View memories with a specific villager.
- `villager`: Target villager

## Help & Tutorials

### `/dating help [command]`
Show command help.
- `command`: Specific command (optional)

### `/dating tutorial`
Start the dating tutorial.

### `/help`
Show general help menu.

## Tips
- Use Tab to autocomplete commands and arguments
- Most commands work with both players and NPCs
- Some commands require certain relationship levels
- Check your achievements for special unlocks
- Higher relationship levels unlock new dialogue options
- Use `/dating help` for detailed command information
- Block players who harass you with `/block <player>`
- View your relationship history with `/memories list`
