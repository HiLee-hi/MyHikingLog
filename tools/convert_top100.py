#!/usr/bin/env python3
"""숲나들e 100대명산 CSV -> assets/top100_mountains.json

Source: 산림청 국립자연휴양림관리소_숲나들e 숲길 100대명산 정보 (data.go.kr 15112801, 이용허락범위 제한 없음)

    python3 tools/convert_top100.py data/source/top100_mountains_20250421.csv \
        android/app/src/main/assets/top100_mountains.json

courses: HTML-escaped table -> unescape, strip tags, split per course
"난이도" column: time / height / difficulty in one string -> split
"""
import csv
import html
import json
import re
import sys

PROVINCES = [
    ("서울특별시", "서울"), ("부산광역시", "부산"), ("대구광역시", "대구"), ("인천광역시", "인천"),
    ("광주광역시", "광주"), ("대전광역시", "대전"), ("울산광역시", "울산"), ("세종특별자치시", "세종"),
    ("경기도", "경기"), ("강원특별자치도", "강원"), ("강원도", "강원"), ("충청북도", "충북"),
    ("충청남도", "충남"), ("전북특별자치도", "전북"), ("전라북도", "전북"), ("전라남도", "전남"),
    ("경상북도", "경북"), ("경상남도", "경남"), ("제주특별자치도", "제주"),
]


# line breaks in the CSV became runs of spaces. breaks:
#   "sentence": 2+ spaces after . ! ? ] or before a list item -> \n (prose has some stray double spaces)
#   "any": every 2+ space run -> \n (transport is mostly lists)
def clean(text: str, breaks: str = "") -> str:
    text = html.unescape(html.unescape(text))
    text = re.sub(r"<br\s*/?>", "\n", text, flags=re.I)
    text = re.sub(r"<[^>]+>", " ", text)
    text = re.sub(r"[\t\r ]", " ", text)
    if breaks == "sentence":
        text = re.sub(r"(?<=[.!?\]]) {2,}| {2,}(?=[-→>])", "\n", text)
    elif breaks == "any":
        text = re.sub(r" {2,}", "\n", text)
    text = re.sub(r" +", " ", text)
    text = re.sub(r" *\n *", "\n", text)
    return text.strip()


def regions(address: str) -> list:
    found = []
    for full, short in PROVINCES:
        if full in address and short not in found:
            found.append(short)
    return found


def minutes(duration: str):
    hours = re.search(r"(\d+)\s*시간", duration)
    mins = re.search(r"(\d+)\s*분", duration)
    if not hours and not mins:
        return None
    return int(hours.group(1) if hours else 0) * 60 + int(mins.group(1) if mins else 0)


def courses(raw: str) -> list:
    table = html.unescape(html.unescape(raw))
    result = []
    for row in re.findall(r"<tr>(.*?)</tr>", table, flags=re.S | re.I):
        cells = re.findall(r"<td[^>]*>(.*?)</td>", row, flags=re.S | re.I)
        if len(cells) < 3:
            continue
        label, route, duration = (re.sub(r"\s+", " ", clean(c)).strip() for c in cells[:3])
        result.append({"label": label, "route": route, "duration": duration, "minutes": minutes(duration)})
    return result


def main(src: str, dst: str) -> None:
    rows = list(csv.DictReader(open(src, encoding="utf-8-sig")))
    out = []
    for r in rows:
        meta = r["난이도"]
        time_range = re.search(r"산행시간\s*:\s*(.*?)\s*산높이", meta).group(1).strip()
        difficulty = re.search(r"난이도\s*:\s*(\S+)", meta).group(1).strip()
        address = clean(r["명산_소재지"])
        out.append({
            "id": "top100:" + r["명산_이름"].strip(),
            "name": r["명산_이름"].strip(),
            "address": address,
            "regions": regions(address),
            "heightMeters": float(r["명산_높이"]),
            "latitude": float(r["Y좌표"]),
            "longitude": float(r["X좌표"]),
            "hikingTime": None if time_range == "-" else time_range,
            "officialDifficulty": None if difficulty == "-" else difficulty,
            "selectionReason": clean(r["특징_및_선정_이유"]),
            "overview": clean(r["산_개요"], "sentence"),
            "hikingPoints": clean(r["산행포인트"], "sentence"),
            "courses": courses(r["산행코스"]),
            "transport": clean(r["교통정보"], "any"),
        })
    with open(dst, "w", encoding="utf-8") as f:
        json.dump(out, f, ensure_ascii=False, indent=1)
    print(f"wrote {len(out)} mountains to {dst}")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
