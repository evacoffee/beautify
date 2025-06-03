# BeautyMod API Documentation

## Overview
This document describes how to interact with BeautyMod's API for other mod developers.

## Setup
Add BeautyMod as a dependency in your `build.gradle`:

```groovy
dependencies {
    modImplementation "com.example:beautymod:${project.beautymod_version}"
    include "com.example:beautymod:${project.beautymod_version}"
}
```

## Core Features

### Relationship System
```java
// Get relationship level with an entity
IRelationshipComponent relationship = RelationshipComponents.RELATIONSHIP.get(entity);
int friendshipLevel = relationship.getFriendshipLevel(player.getUuid());

// Modify relationship
relationship.modifyFriendship(player.getUuid(), 10); // Increase by 10 points
```

### Events
```java
// Listen for relationship changes
RelationshipCallback.EVENT.register((source, target, amount) -> {
    // Handle relationship change
    return ActionResult.PASS;
});

// Listen for marriage events
MarriageEvents.MARRIAGE_EVENT.register((player1, player2) -> {
    // Handle marriage
});
```

## Configuration
Access the mod's configuration:

```java
BeautyModConfig config = BeautyMod.getConfig();
boolean enableGifts = config.isGiftSystemEnabled();
```

## Permissions
Check player permissions:

```java
// Check if player can use a feature
boolean canUseFeature = Permissions.check(player, "beautymod.feature.use", false);
```