#!/usr/bin/env python3
# Checks the immutability rules that ArchitectureTest's fieldsAreFinal can't see in bytecode. Run from the repo root:
#   python3 -I .claude/skills/immutable-java/check.py [files...]
# Without files it checks every main source under macro-backend/. Exits 1 on an error; warnings don't fail it.
import pathlib
import re
import sys

COLLECTION = re.compile(r'^(List|Set|Map|Collection)<')
COPIES = re.compile(r'ImmutableCopies\.\w+\(|Collections\.unmodifiable\w+\(')


def main_sources(args):
    if args:
        return [pathlib.Path(a) for a in args if a.endswith('.java') and '/src/main/' in a and 'jooq/generated' not in a]
    return [p for p in pathlib.Path('macro-backend').glob('*/src/main/java/**/*.java') if 'jooq/generated' not in str(p)]


def split_top_level(text):
    parts, depth, current = [], 0, ''
    for ch in text:
        depth += ch in '<(' 
        depth -= ch in '>)'
        if ch == ',' and depth == 0:
            parts.append(current)
            current = ''
        else:
            current += ch
    return parts + [current]


def closing_paren(text, start):
    depth = 1
    i = start
    while depth:
        depth += {'(': 1, ')': -1}.get(text[i], 0)
        i += 1
    return i


def record_errors(path, source):
    errors = []
    for match in re.finditer(r'\brecord\s+(\w+)\s*(?:<[^>]*>)?\s*\(', source):
        name = match.group(1)
        end = closing_paren(source, match.end())
        header = re.sub(r'//[^\n]*', '', source[match.end():end - 1])
        header = re.sub(r'@\w+(\([^)]*\))?', '', header)
        components = []
        for part in split_top_level(header):
            part = part.strip()
            if part:
                type_, component = part.rsplit(None, 1)
                if COLLECTION.match(type_.strip()):
                    components.append(component)
        if not components:
            continue
        compact = re.search(r'\n\s*public\s+' + name + r'\s*\{(.*?)\n\s*\}\n', source[end:], re.S)
        if not compact:
            errors.append(f'{path}: record {name} has collection components {components} but no compact constructor')
            continue
        for component in components:
            if not re.search(r'\b' + component + r'\s*=\s*(?:' + COPIES.pattern + ')', compact.group(1)):
                errors.append(f'{path}: record {name} does not copy or wrap {component} in its compact constructor')
    return errors


def line_findings(path, source):
    errors, warnings = [], []
    for number, line in enumerate(source.splitlines(), 1):
        where = f'{path}:{number}'
        if re.search(r'@(Data|Setter|With)\b', line):
            errors.append(f'{where}: Lombok @Data/@Setter/@With makes a value mutable; use a record and toBuilder()')
        if re.search(r'Collectors\.(toList|toSet)\(\)', line):
            warnings.append(f'{where}: Collectors.toList()/toSet() are mutable; use .toList() or Collectors.toUnmodifiableSet()')
        if re.search(r'return new (ArrayList|LinkedList|HashMap|LinkedHashMap|HashSet|LinkedHashSet|TreeMap|TreeSet)\b', line):
            warnings.append(f'{where}: returns a mutable collection; return it unmodifiable')
    return errors, warnings


def main():
    errors, warnings = [], []
    for path in main_sources(sys.argv[1:]):
        source = path.read_text(encoding='utf-8')
        errors += record_errors(path, source)
        line_errors, line_warnings = line_findings(path, source)
        errors += line_errors
        warnings += line_warnings
    for warning in warnings:
        print('warning:', warning)
    for error in errors:
        print('error:', error)
    print(f'{len(errors)} errors, {len(warnings)} warnings')
    sys.exit(1 if errors else 0)


main()
