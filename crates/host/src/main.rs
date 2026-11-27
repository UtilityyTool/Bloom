//! A command-line demo of the Bloom host harness.
//!
//!   cargo run -p bloom-host -- [path-to-plugin-folder]
//!
//! With no path it runs the bundled `plugins/clean-links` example. It shows the whole
//! pipeline: read, check/verify, enforce permissions, run — then a short demo of the
//! Verified badge using a throwaway Bloom key.

use std::path::{Path, PathBuf};

use bloom_host::{load_plugin_dir, LoadedPlugin};
use bloom_runtime::manifest::Risk;
use bloom_runtime::{Event, PluginManifest};
use bloom_runtime::js::Message;
use bloom_signing::{digest_plugin, generate_keypair, sign, trust_of, Source, Trust};

fn main() {
    let dir = std::env::args()
        .nth(1)
        .map(PathBuf::from)
        .unwrap_or_else(default_example_dir);

    println!("Bloom host harness");
    println!("==================\n");

    let mut loaded = match load_plugin_dir(&dir, Source::LocalFolder, None) {
        Ok(p) => p,
        Err(e) => {
            eprintln!("Could not load plugin at {}: {e}", dir.display());
            std::process::exit(1);
        }
    };

    report_header(&loaded);
    run_samples(&mut loaded);
    verified_badge_demo(&dir);
}

fn report_header(loaded: &LoadedPlugin) {
    let m = &loaded.manifest;
    println!("Plugin:  {} {}  (by {})", m.name, m.version, if m.author.is_empty() { "unknown" } else { &m.author });
    println!("From:    local folder  ->  trust: {}", trust_label(loaded.trust));
    println!("Digest:  {}", &loaded.digest_hex[..16]);
    if m.permissions.is_empty() && m.network.is_empty() {
        println!("Access:  none");
    } else {
        println!("Access:");
        for p in &m.permissions {
            println!("   - {}  [{}]", p.label(), risk_label(p.risk()));
        }
        for host in &m.network {
            println!("   - Sends data to {host}  [high]");
        }
    }
    println!();
}

fn run_samples(loaded: &mut LoadedPlugin) {
    // A message with a tracker-laden link, as if the user were about to send it.
    let mut msg = Message {
        text: "check this out youtube.com/watch?v=abc&si=track123".into(),
    };
    println!("Sample: user sends \"{}\"", msg.text);

    let before = msg.text.clone();
    let (out, verdict) = loaded.run(Event::BeforeSend, &mut msg);

    if msg.text != before {
        println!("  -> plugin changed it to \"{}\"", msg.text);
    } else {
        println!("  -> plugin left it unchanged");
    }
    for line in &out.logs {
        println!("  log: {line}");
    }
    if let Some(err) = &out.error {
        println!("  blocked: {err}");
    }
    println!("  ran in {:?}  ({})", out.elapsed, verdict_label(verdict));
    println!();
}

// Show that signing makes the Verified badge real, using a throwaway Bloom key.
fn verified_badge_demo(dir: &Path) {
    let manifest_json = std::fs::read_to_string(dir.join("plugin.json")).unwrap_or_default();
    let code = std::fs::read_to_string(dir.join("index.js"))
        .or_else(|_| std::fs::read_to_string(dir.join("dist/index.js")))
        .unwrap_or_default();
    if manifest_json.is_empty() {
        return;
    }
    // sanity: manifest parses
    let _ = PluginManifest::parse(&manifest_json);

    let bloom = generate_keypair();
    let digest = digest_plugin(&manifest_json, &code);
    let sig = sign(&bloom.signing, &digest);

    println!("Verified-badge check (using a throwaway Bloom key):");
    let t = trust_of(Source::Store, &digest, Some(&sig), &bloom.verifying);
    println!("  store copy, signed by Bloom      -> {}", trust_label(t));

    // Tamper: same signature, but the code changed by one line.
    let tampered = digest_plugin(&manifest_json, &format!("{code}\n// sneaky change"));
    let t2 = trust_of(Source::Store, &tampered, Some(&sig), &bloom.verifying);
    println!("  same copy after a tiny edit      -> {}  (badge lost)", trust_label(t2));
}

fn default_example_dir() -> PathBuf {
    Path::new(env!("CARGO_MANIFEST_DIR"))
        .join("../../plugins/clean-links")
        .canonicalize()
        .unwrap_or_else(|_| PathBuf::from("plugins/clean-links"))
}

fn trust_label(t: Trust) -> &'static str {
    match t {
        Trust::Verified => "Verified \u{2713}",
        Trust::Community => "Community (unverified)",
        Trust::Developer => "Developer (local)",
    }
}

fn risk_label(r: Risk) -> &'static str {
    match r {
        Risk::Low => "low",
        Risk::Medium => "medium",
        Risk::High => "high",
    }
}

fn verdict_label(v: bloom_runtime::Verdict) -> &'static str {
    match v {
        bloom_runtime::Verdict::Ok => "within budget",
        bloom_runtime::Verdict::Warned => "over one frame",
        bloom_runtime::Verdict::Slow => "flagged slow",
        bloom_runtime::Verdict::Paused => "paused (too slow)",
    }
}
