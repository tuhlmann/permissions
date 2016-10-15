# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]
### Changed
- xxx.

## [0.1.2-SNAPSHOT] - 2016-10-15
### Changed
- Changed using keywords for domain, actions or entities to using plain strings.
  This creates less confusion and friction with db load/unload or use 
  across Clojure / Clojurescript.
- change atoms to store the role map and the resolvers from `defonce` to `def` so they could be
  easier used in a module.

## [0.1.1-SNAPSHOT] - 2016-10-15
### Added
- Ability to add bitmasks to users and resources instead of literal permission strings.

### Fixed
- Some of the unit tests declared the permission string wrong

## [0.1.0-SNAPSHOT] - 2016-10-09
### Added
- Initial Revision.

[Unreleased]: https://github.com/tuhlmann/permissions/compare/0.1.1...HEAD
[0.1.1]: https://github.com/tuhlmann/permissions/compare/0.1.0...0.1.1
