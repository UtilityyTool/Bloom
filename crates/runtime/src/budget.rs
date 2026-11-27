//! The performance guard. Discord on a 120 Hz phone has ~8 ms per frame, so a plugin
//! that runs long makes scrolling stutter. Every plugin handler is timed against this.

use std::time::Duration;

/// Informational: handlers should aim to stay under this.
pub const SOFT: Duration = Duration::from_millis(2);
/// One frame's budget. Going over this is a warning.
pub const HARD: Duration = Duration::from_millis(8);
/// A single call this slow pauses the plugin outright.
pub const PAUSE: Duration = Duration::from_millis(50);
/// This many over-HARD calls and the plugin is flagged "Slow" in the menu.
pub const SLOW_AFTER: u32 = 5;

/// What timing a single handler run earned.
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Verdict {
    /// Within budget.
    Ok,
    /// Over one frame, but not yet a pattern.
    Warned,
    /// Over budget often enough to flag to the user.
    Slow,
    /// One call so slow the plugin is paused.
    Paused,
}

/// Tracks how a single plugin is doing against the budget.
#[derive(Debug, Default, Clone)]
pub struct Budget {
    over_hard: u32,
}

impl Budget {
    /// Record one handler run and say what it earned.
    pub fn record(&mut self, elapsed: Duration) -> Verdict {
        if elapsed >= PAUSE {
            return Verdict::Paused;
        }
        if elapsed > HARD {
            self.over_hard += 1;
            return if self.over_hard >= SLOW_AFTER {
                Verdict::Slow
            } else {
                Verdict::Warned
            };
        }
        Verdict::Ok
    }

    /// How many times this plugin has gone over one frame's budget.
    pub fn over_hard_count(&self) -> u32 {
        self.over_hard
    }
}
