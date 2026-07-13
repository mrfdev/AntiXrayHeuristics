# Placeholders

`1MB XRayHeuristics` does not register verified PlaceholderAPI expansion identifiers. The placeholders below are internal placeholders used in config-driven commands, locale strings, and vault GUI text.

## Config Command Placeholders

| Placeholder | Used in | Meaning |
| --- | --- | --- |
| `{PlayerName}` | `CommandsExecutedOnXrayerDetected`, `CommandsExecutedOnPlayerAbsolved`, several locale messages | The handled or targeted player's name. |
| `{TimesDetected}` | `CommandsExecutedOnXrayerDetected` | The handled-count bucket returned by the persistence layer for that player. |

## Vault and Locale Placeholders

| Placeholder | Used in | Meaning |
| --- | --- | --- |
| `{HandledTimesAmount}` | `EntryDesc`, `EntryDescInspector` | Number of times that stored entry has been handled. |
| `{FirstTimeDetected}` | `EntryDesc`, `EntryDescInspector` | First recorded handle timestamp for the stored entry. |
| `{LastSeenTime}` | `EntryDesc`, `EntryDescInspector` | Latest formatted “last seen” time shown in the vault GUI. |

## Notes

- `HandledXrayerSlotName` in `locale.yml` contains `#{Slot}`, but there is no verified substitution helper for `{Slot}` in the current source. It should not be documented as an active, supported placeholder until the code implements it.
- The plugin uses Adventure's legacy serializer to preserve color codes when replacing its internal placeholders.
