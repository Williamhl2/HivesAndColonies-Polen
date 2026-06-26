from __future__ import annotations

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
ASSET_ROOT = ROOT / "src" / "main" / "resources" / "assets" / "hc_characters"
LANG_BASE_DIR = ASSET_ROOT / "lang_base"
LANG_PARTS_DIR = ASSET_ROOT / "lang_parts"
LANG_OUTPUT_DIR = ASSET_ROOT / "lang"


def read_json_map(path: Path) -> dict[str, str]:
    if not path.exists():
        return {}

    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, dict):
        raise ValueError(f"Expected JSON object in {path}")
    return dict(data)


def sorted_json_files(root: Path) -> list[Path]:
    if not root.exists():
        return []
    return sorted(path for path in root.rglob("*.json") if path.is_file())


def fallback_locales(locale: str, available_locales: set[str]) -> list[str]:
    fallbacks: list[str] = []
    if locale != "en_us" and "en_us" in available_locales:
        fallbacks.append("en_us")
    if locale.startswith("es_") and locale != "es_es" and "es_es" in available_locales:
        fallbacks.append("es_es")
    return [item for item in dict.fromkeys(fallbacks) if item != locale]


def collect_locales() -> list[str]:
    locales = {path.stem for path in LANG_BASE_DIR.glob("*.json")}
    locales.update(path.name for path in LANG_PARTS_DIR.iterdir() if path.is_dir())
    return sorted(locales)


def merge_sources_for_locale(locale: str) -> dict[str, str]:
    merged: dict[str, str] = {}
    sources: list[Path] = []

    base_file = LANG_BASE_DIR / f"{locale}.json"
    if base_file.exists():
        sources.append(base_file)

    sources.extend(sorted_json_files(LANG_PARTS_DIR / locale))
    if not sources:
        raise ValueError(f"Locale '{locale}' has no source files.")

    for source in sources:
        data = read_json_map(source)
        overlap = merged.keys() & data.keys()
        if overlap:
            sample = ", ".join(sorted(overlap)[:10])
            raise ValueError(f"Duplicate keys for locale '{locale}' in {source}: {sample}")
        merged.update(data)

    return merged


def build_locale_payloads() -> dict[str, dict[str, str]]:
    locales = collect_locales()
    merged_by_locale = {locale: merge_sources_for_locale(locale) for locale in locales}
    available_locales = set(merged_by_locale)

    final_by_locale: dict[str, dict[str, str]] = {}
    for locale, entries in merged_by_locale.items():
        merged_entries: dict[str, str] = {}
        for fallback in fallback_locales(locale, available_locales):
            merged_entries.update(merged_by_locale[fallback])
        merged_entries.update(entries)
        final_by_locale[locale] = dict(sorted(merged_entries.items()))

    baseline_locale = "en_us" if "en_us" in final_by_locale else locales[0]
    baseline_keys = set(final_by_locale[baseline_locale].keys())

    for locale, entries in final_by_locale.items():
        missing = sorted(baseline_keys - set(entries.keys()))
        if missing:
            sample = ", ".join(missing[:10])
            raise ValueError(
                f"Locale '{locale}' is missing {len(missing)} keys required by '{baseline_locale}': {sample}"
            )

    return final_by_locale


def write_output(locale_payloads: dict[str, dict[str, str]]) -> None:
    LANG_OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    expected_files = set()

    for locale, payload in locale_payloads.items():
        target = LANG_OUTPUT_DIR / f"{locale}.json"
        target.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        expected_files.add(target.name)

    for stale in LANG_OUTPUT_DIR.glob("*.json"):
        if stale.name not in expected_files:
            stale.unlink()


def main() -> None:
    locale_payloads = build_locale_payloads()
    write_output(locale_payloads)
    print(f"Wrote {len(locale_payloads)} merged locale files to {LANG_OUTPUT_DIR}")


if __name__ == "__main__":
    main()
