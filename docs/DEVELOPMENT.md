# Development Guide

## Prerequisites
- Java 17 or higher
- IntelliJ IDEA (recommended) or VS Code
- Fabric Loom

## Setup
1. Clone the repository
2. Run `./gradlew genSources` (Linux/macOS) or `gradlew genSources` (Windows)
3. Import into your IDE as a Gradle project

## Project Structure
```
src/main/
  ├── java/com/example/beautymod/
  │   ├── api/           # Public API for other mods
  │   ├── config/        # Configuration files
  │   ├── entity/        # Custom entities
  │   ├── event/         # Event handlers
  │   ├── network/       # Network packets
  │   ├── registry/      # Registry handlers
  │   ├── system/        # Core systems
  │   └── util/          # Utility classes
  └── resources/         # Assets and data files
```

## Code Style
- Follow the [Fabric Community Guidelines](https://fabricmc.net/community/guidelines/)
- Use 4 spaces for indentation
- Class names in PascalCase
- Methods and variables in camelCase
- Constants in UPPER_SNAKE_CASE

## Testing
Run tests with:
```bash
./gradlew test
```

## Pull Requests
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests
5. Submit a PR with a clear description

## Versioning
We follow [Semantic Versioning](https://semver.org/):
- MAJOR: Incompatible API changes
- MINOR: Backwards-compatible functionality
- PATCH: Backwards-compatible bug fixes
- Include examples for complex functions
- Keep comments up to date

## Testing

### Unit Tests
- Test all security-critical code
- Mock external dependencies
- Test edge cases

### Integration Tests
- Test component interactions
- Verify security controls
- Test with realistic data

## Dependencies

### Adding Dependencies
- Prefer official libraries
- Check for active maintenance
- Review security history

### Updating Dependencies
- Update regularly
- Test thoroughly
- Review changelogs

## Release Process

1. Run all tests
2. Update version numbers
3. Update changelog
4. Create release notes
5. Tag the release
6. Publish artifacts