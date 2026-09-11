# SCore

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7dfab7ca1a8e478e91098bd3a5f9d217)](https://app.codacy.com/gh/Ssomar-Developement/SCore?utm_source=github.com&utm_medium=referral&utm_content=Ssomar-Developement/SCore&utm_campaign=Badge_Grade_Settings)
[![](https://jitpack.io/v/Ssomar-Developement/SCore.svg)](https://jitpack.io/#Ssomar-Developement/SCore)

## Overview

SCore is a comprehensive library and framework for Minecraft plugins, developed primarily to support Ssomar's plugin ecosystem. It provides a rich set of tools and utilities that simplify the development of advanced Minecraft plugins.

### Key Features

- **Custom Commands System**: Extensible command framework supporting player, block, and entity commands
- **Condition Engine**: Advanced condition system for complex logic evaluation
- **Variables System**: Dynamic variable management with persistent storage
- **Particle System**: Comprehensive particle effects and display management
- **In-Game Editor**: Efficient GUI-based editor framework for runtime configuration
- **Multi-Version Support**: Compatible with Minecraft 1.8+ through 1.21.6+
- **Server Platform Support**: Works with Spigot, Paper, Folia, and other server implementations
- **Plugin Integration**: Seamless integration with 40+ popular plugins including WorldGuard, PlaceholderAPI, Vault, and more

## Architecture

SCore serves as both a standalone plugin and a library that can be integrated into other plugins. It provides:

- **Core Utilities**: Scheduler hooks, database management, event handling
- **Feature Framework**: Extensible feature system with built-in editor support
- **Plugin APIs**: Integration layers for popular plugins
- **Data Management**: MySQL and file-based storage systems
- **Display Systems**: Advanced item and GUI display management

## Requirements

- **Java**: JDK 8 or newer
- **Server**: Spigot 1.8+ or Paper/Folia equivalent
- **Dependencies**: See `pom.xml` for complete dependency list
- **Knowledge**: Basic understanding of Bukkit/Spigot plugin development and Maven

## Building from Source

### Prerequisites

1. Install Java JDK 8 or newer
2. Install Maven 3.6+
3. Clone the repository

### Build Commands

```bash
# Clean build and install to local repository
mvn clean package install

# Build with specific profile (for developers)
mvn clean package -PSsomar

# Build without tests
mvn clean package -DskipTests
```

### Build Output

The compiled JAR will be located in `target/SCore-{version}.jar`

## Installation

1. Download the latest release from [Modrinth](https://modrinth.com/plugin/score)
2. Place `SCore.jar` in your server's `plugins` folder
3. Restart your server
4. Configure the plugin using `/score` commands

## Usage

### As a Plugin

SCore can be used as a standalone plugin providing:
- `/score reload` - Reload configuration
- `/score inspect-loop` - Debug loop performance
- Variables management
- Projectile system
- Custom hardness system

### As a Library

Include SCore in your plugin's dependencies:

```xml
<dependency>
    <groupId>com.ssomar</groupId>
    <artifactId>SCore</artifactId>
    <version>42.42.42</version>
    <scope>provided</scope>
</dependency>
```

## Contributing

We welcome contributions to SCore! Please follow these guidelines:

### Getting Started

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes
4. Test thoroughly across different Minecraft versions
5. Commit your changes: `git commit -m 'Add amazing feature'`
6. Push to the branch: `git push origin feature/amazing-feature`
7. Open a Pull Request

### Pull Requests

1. **Fork** the repository and create a feature branch
2. **Separate concerns**: Keep bug fixes and new features in separate PRs
3. **Test thoroughly**: Ensure your changes work across supported versions
4. **Document**: Update relevant documentation and comments
5. **Follow conventions**: Match existing code style and patterns

### Development Guidelines

#### Adding Custom Commands

Location: `com.ssomar.score.commands.runnable`

1. Create a new class in the appropriate subpackage
2. Extend the relevant base class (e.g., `MixedCommand.java` for mixed player/entity commands)
3. Implement required methods and registration logic
4. Add appropriate tests and documentation

#### Adding Custom Conditions

Location: `com.ssomar.score.features.custom.conditions`

1. Create a new condition class extending the base condition framework
2. Implement evaluation logic and editor integration
3. Register the condition in the appropriate manager
4. Add unit tests covering edge cases

#### Adding Plugin Integrations

Location: `com.ssomar.score.usedapi`

1. Create a new API wrapper class
2. Add dependency detection in `Dependency.java`
3. Register the integration in `SCore.loadDependency()`
4. Handle version compatibility and graceful degradation

#### Adding Placeholders

SCore provides an extensive placeholder system that integrates with PlaceholderAPI. You can add new placeholders in several ways:

**Location**: `com.ssomar.score.utils.placeholders`

##### Method 1: Extend Existing Placeholder Categories

To add placeholders to existing categories (player, entity, block, etc.):

1. **Modify the appropriate Abstract class** (e.g., `PlayerPlaceholdersAbstract.java`)
2. **Add new fields** for the data you want to expose
3. **Update the `reload*PlcHldr()` method** to populate the new fields
4. **Update the `replacePlaceholder()` method** to handle the new placeholders

##### Method 2: Create New Placeholder Category

For entirely new placeholder categories:

1. **Create a new class** extending `PlaceholdersInterface`
2. **Implement required methods**: `replacePlaceholder()`, `reload*PlcHldr()`
3. **Add to StringPlaceholder class** for integration

##### Method 3: Add PlaceholderAPI Integration

To add placeholders accessible via PlaceholderAPI (`%SCore_*%`):

1. **Modify `PlaceholderAPISCoreExpansion.java`**
2. **Add new logic in `onRequest()` method**

##### Available Placeholder Categories

- **Player**: `%player_*%` - Player data (health, location, name, etc.)
- **Entity**: `%entity_*%` - Entity data (type, health, location, etc.)
- **Block**: `%block_*%` - Block data (type, location, world, etc.)
- **Target**: `%target_*%` - Target player/entity/block data
- **Around**: `%around_*%` - Entities/players around a target
- **Effect**: `%effect_*%` - Potion effect data
- **Time**: `%time_*%` - Time formatting
- **Projectile**: `%projectile_*%` - Projectile data
- **Owner**: `%owner_*%` - Owner data for owned objects
- **Variables**: `%SCore_variables_*%` - Custom variables
- **Arithmetic**: Support for `+`, `-`, `*`, `/` operations in placeholders

### Code Style

- Follow existing naming conventions
- Use proper JavaDoc for public APIs
- Keep methods focused and well-documented
- Handle exceptions appropriately
- Support multi-version compatibility where applicable

### Testing

1. Test across multiple Minecraft versions (1.8, 1.12, 1.16, 1.20, 1.21)
2. Verify compatibility with both Spigot and Paper
3. Test plugin integrations if applicable
4. Check performance impact of changes

## License

This project is licensed under the terms specified in the repository. Please review the license file before contributing.

## Support

- **Issues / Feature request**: For bug reports and feature requests join our [Discord](https://discord.com/invite/TRmSwJaYNv)
- **Community**: Join our [Discord](https://discord.com/invite/TRmSwJaYNv) community server for support and discussions
- **Documentation**: Additional documentation available in the codebase or in the [Wiki](https://splugins.net/docs/tools-for-all-plugins-score/custom-commands)

## Related Projects

SCore powers several other plugins in the Ssomar ecosystem:
- ExecutableItems
- ExecutableBlocks
- ExecutableEvents
- CustomPiglinsTrades
- MyFurniture
- ExecutableCrafting

---

*Developed with ❤️ by Ssomar and the contributors*

