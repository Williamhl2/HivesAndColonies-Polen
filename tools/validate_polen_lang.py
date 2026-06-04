#!/usr/bin/env python3
import json
from pathlib import Path

root = Path('src/main/resources/assets/polen')
base_root = root / 'lang_base'
parts_root = root / 'lang_parts'

locales = sorted(
    {
        *(p.stem for p in base_root.glob('*.json')),
        *(p.name for p in parts_root.iterdir() if p.is_dir()),
    }
)
all_keys = {}

for locale in locales:
    merged = {}
    base_file = base_root / f'{locale}.json'
    files = []
    if base_file.exists():
        files.append(base_file)

    parts_dir = parts_root / locale
    if parts_dir.exists():
        files.extend(sorted(parts_dir.glob('*.json')))

    if not files:
        raise SystemExit(f'{locale}: no source files found')

    for file in files:
        with file.open(encoding='utf-8') as handle:
            data = json.load(handle)
        overlap = set(merged).intersection(data)
        if overlap:
            raise SystemExit(f'{locale}: duplicate keys in {file.name}: {sorted(overlap)[:10]}')
        merged.update(data)
    all_keys[locale] = set(merged)

resolved_keys = {}


def fallback_locales_for(locale: str) -> list[str]:
    fallbacks: list[str] = []
    if locale != 'en_us' and 'en_us' in all_keys:
        fallbacks.append('en_us')
    if locale.startswith('es_') and locale != 'es_es' and 'es_es' in all_keys:
        fallbacks.append('es_es')
    return [value for value in fallbacks if value != locale]


for locale, keys in all_keys.items():
    resolved = set()
    for fallback in fallback_locales_for(locale):
        resolved.update(all_keys[fallback])
    resolved.update(keys)
    resolved_keys[locale] = resolved

baseline_name = 'en_us' if 'en_us' in resolved_keys else locales[0]
baseline = resolved_keys[baseline_name]
for locale, keys in resolved_keys.items():
    missing = sorted(baseline - keys)
    if missing:
        raise SystemExit(f'{locale}: missing={missing[:20]}')

print(f'OK: {len(baseline)} required keys present in {len(locales)} locales; extras allowed')
