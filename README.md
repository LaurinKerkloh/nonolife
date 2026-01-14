# NoNoLife

This is a fabric server side mod to limit daily playtime on a minecraft server.
So no one can "no-life" the game.

## How it works

Each player has some playtime available, while playing your available playtime is counted down.
When it reaches 0 you get kicked from the server.
Every day a configurable amount of playtime is granted to all players.
When playtime is not used up it stacks up to a configurable limit.

Remaining playtime is indicated with a bar at the top of the screen.

Total playtime is also tracked and displayed in the player (tab) list.

## Installation

This mod is fully server side, so no client mod required!

Make sure you have the [Fabric API](https://modrinth.com/mod/fabric-api),
then just add the `nonolife-X.X.X.jar` file to your server's mod folder and off you go.

## Config

To configure the mod to your liking create a file json in your servers `config` folder.
The file should be called `nonolife.json`

Example file content (with default values):

```json
{
  "dailyPlaytime": 3600,
  "initialPlaytime": 3600,
  "maximumPlaytime": 14400,
  "addPlaytimeAtHour": 4,
  "showTotalPlaytime": true
}
```

- dailyPlaytime sets the playtime in seconds each player is granted every day (default: 3600)
- initialPlaytime sets the playtime in seconds a player is granted when he/she first joins the server (default: 3600)
- maximumPlaytime sets the maximum amount of playtime in seconds that one can accumulate (default: 3600)
- addPlaytimeAtHour sets at which full hour the daily playtime is granted (default: 4)
- showTotalPlaytime controlls if the total playtime is shown in the player (tab) list (default: true)

If you want to use a default value you do not have to provide the configuration key. If you want to use all defaults you can just not create the file at all.

## Commands

- `/playtime hide` hides the remaining playtime indicator bar
- `/playtime show` shows the remaining playtime indicator bar
