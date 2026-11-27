//! Review a plugin folder from the command line.
//!
//!   cargo run -p bloom-review -- [path-to-plugin-folder]
//!
//! Prints the findings and the verdict. Exit code is non-zero if the plugin fails.

use std::path::{Path, PathBuf};

use bloom_review::{review, Severity, Verdict};
use bloom_runtime::PluginManifest;

fn main() {
    let dir = std::env::args()
        .nth(1)
        .map(PathBuf::from)
        .unwrap_or_else(|| {
            Path::new(env!("CARGO_MANIFEST_DIR"))
                .join("../../plugins/clean-links")
                .canonicalize()
                .unwrap_or_else(|_| PathBuf::from("plugins/clean-links"))
        });

    let manifest_json = read(&dir, "plugin.json");
    let code = std::fs::read_to_string(dir.join("index.js"))
        .or_else(|_| std::fs::read_to_string(dir.join("dist/index.js")))
        .unwrap_or_default();

    let manifest = match PluginManifest::parse(&manifest_json) {
        Ok(m) => m,
        Err(e) => {
            eprintln!("Can't read plugin.json: {e}");
            std::process::exit(2);
        }
    };

    let r = review(&manifest, &code);
    println!("Reviewing {} {}\n", manifest.name, manifest.version);

    if r.findings.is_empty() {
        println!("  (no findings)");
    }
    for f in &r.findings {
        println!("  {:<8} {}", tag(f.severity), f.message);
    }

    println!();
    match r.verdict {
        Verdict::Pass => {
            println!("Verdict: PASS — safe to verify automatically.");
        }
        Verdict::NeedsHumanReview => {
            println!("Verdict: NEEDS HUMAN REVIEW — nothing disqualifying, but a person should look.");
        }
        Verdict::Fail => {
            println!("Verdict: FAIL — can't be verified as-is.");
            std::process::exit(1);
        }
    }
}

fn read(dir: &Path, name: &str) -> String {
    std::fs::read_to_string(dir.join(name)).unwrap_or_default()
}

fn tag(s: Severity) -> &'static str {
    match s {
        Severity::Note => "note",
        Severity::Warning => "warning",
        Severity::Error => "ERROR",
    }
}
