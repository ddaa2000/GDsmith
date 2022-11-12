from lark import Lark

DATABASE_LOG_FILE = 'database2-cur.log'
CYPHER_GRAMMAR_FILE = 'cypher_grammar_rewrite.txt'

with open(CYPHER_GRAMMAR_FILE, 'r', encoding='utf-8') as f:
    t = f.read()

lk = Lark(t)

all_pairs = set()

def add_query(q):
    res = lk.parse(q)

    def is_leave(x):
        return len(x.children) == 0

    def dfs(i):
        for c in i.children:
            if not hasattr(c, 'data'):
                continue
            dfs(c)
            if not is_leave(c):
                p = (i.data.value, c.data.value)
                all_pairs.add(p)

    dfs(res)


all_queries = []
with open(DATABASE_LOG_FILE, 'r', encoding='utf-8') as f:
    for l in f.readlines():
        if '--' in l:
            l = l[:l.find('--')]
        if len(l) == 0:
            continue
        l = l.strip() + ' EOI'
        all_queries.append(l)

all_pairs = set()
for q in all_queries:
    print(q)
    add_query(q)
num = len(all_pairs)
all_pairs = set()
for q in all_queries:
    add_query(q)
    print(len(all_pairs) / num)
