"""
pip install pandas seaborn drain3 matplotlib tqdm
# usage: python log-deal.py [number]        # number: 1-10, default: 1
# example: python log-deal.py 1 : analyse home/***/all-envs/test-env-1/GDsmith-new/
# Output
    - _tmp_env_{number}.json: calculated file
    - _tmp_env_{number}.png: growth plot
"""




import os,sys,re
import time
import json
import pandas as pd
import seaborn as sns #; sns.set()
import matplotlib.pyplot as plt
import drain3
import tempfile
from tqdm import tqdm


timeout_filter = {
    "neo4j":"org.neo4j.driver.exceptions.ClientException: The transaction has been terminated. Retry your operation in a new transaction, and you should see a successful result. The transaction has not completed within the specified timeout (dbms.transaction.timeout). You may want to retry with a longer timeout.",
    'redisgraph':'redis.clients.jedis.exceptions.JedisDataException: Query timed out',
    'memgraph':'org.neo4j.driver.exceptions.TransientException: Transaction was asked to abort, most likely because it was executing longer than time specified by --query-execution-timeout-sec flag.'
}
timeout_filter_regex = {
    "neo4j": re.compile(r".*The transaction has not completed within the specified timeout.*"),
    'redisgraph':re.compile(r".*redis.clients.jedis.exceptions.JedisDataException: Query timed out.*"),
    'memgraph': re.compile(r".*most likely because it was executing longer than time specified by --query-execution-timeout-sec flag*"),
}


GLOBAL_RES = []
def one_env_main(envpath):
    for _ in tqdm(os.listdir(envpath)):
        if re.match(r'^logs-\d+$', _):
            log_deal_dir = os.path.join(envpath, _)
            one_log_dir(os.path.join(log_deal_dir, 'composite'))

def one_log_dir(dirpath):
    """already in composite dir
    Args:
        dirpath (str): already in composite dir, like /test-env-1/GDsmith-new/logs-0/composite
    """
    for _logfile_path in os.listdir(dirpath):
        if "cur" in _logfile_path:
            continue
        else:
            logpath = os.path.join(dirpath,_logfile_path)
            errortype, logtime, firstline_causedby = one_log_file(logpath)
            GLOBAL_RES.append({
                'logpath':logpath,
                'errortype':errortype,
                'logtime':time.strftime(r'%Y/%m/%d %H:%M:%S',logtime),
                'firstline_causedby':firstline_causedby
            })


timeregex = re.compile(r'^---- Time:\s([\d /:]*)')
causedbyregex = re.compile(r'^--Caused by: (.*)')
def _contain_filter_timeout(s):
    res = False
    for reg in timeout_filter_regex.values():
        if reg.findall(s)!=[]:
            res = True
    return res

def one_log_file(logfile_path,filter_timeout=True):
    """_summary_

    Args:
        logfile_path (_type_): _description_
        filter_timeout (bool, optional): _description_. Defaults to True.
    Returns:
        errortype: 'mismatch' / 'crash' / other
        logtime (time.struct_time): 
        firstline_causedby: 'TIMEOUT' / other
    """
    def _print(path,errortype,logtime):
        print(f"===================={path}====================")
        print(f"error type: {errortype}")
        print(f"{time.strftime(r'%Y/%m/%d %H:%M:%S',logtime)}")
    content = []
    logtime = None
    errortype = None # mismatch / crash
    with open(logfile_path, 'r') as f:
        for line in f:
            if line.startswith('--'):
                content.append(line)
            else:
                break
    # determin type
    if "gdsmith.exceptions.ResultMismatchException" in content[0]:
        errortype = 'mismatch'
    elif "gdsmith.exceptions.DatabaseCrashException" in content[0]:
        errortype = 'crash'
    elif "java.lang.RuntimeException: total number reached" in content[0]:
        errortype = 'total number reached'
    elif "java.lang.Exception: a specific database failed" in content[0]:
        errortype = 'a specific database failed'
    else:
        redprint(f"Error in {logfile_path}")
        redprint(f"{content[0]}")
        raise NotImplementedError("Unknown errortype")

# 包含了database
    
    flag_causedby = False
    # list_causedby = []
    firstline_causedby = None
    # search through
    for line in content:
        # get time
        if flag_causedby == True:
            if "Suppressed:" in line:
                flag_causedby = False 
            else:
                # list_causedby.append(line)
                ...
        if (_firstline_causedby := causedbyregex.findall(line)) != []:
            firstline_causedby = _firstline_causedby[0]
            # filter timeout
            if filter_timeout and _contain_filter_timeout(firstline_causedby):
                firstline_causedby = "Timeout"
            # list_causedby.append(line)
            flag_causedby = True
        elif (matchedtime := timeregex.findall(line)) != []:
            logtime = time.strptime(matchedtime[0],r'%Y/%m/%d %H:%M:%S')

    # _print(logfile_path,errortype,logtime)
    # if errortype == 'crash':
    #     redprint(f"*********{firstline_causedby}")
    
    return errortype, logtime, firstline_causedby
    
def redprint(s):
    print(f"\033[31m{s}\033[0m")
def blueprint(s):
    print(f"\033[34m{s}\033[0m")


def get_test_env_dir_by_id(id:int):
    return f"/home/huaziyue/all-envs/experiment-result/test-env-{id}"
TEST_ENV_DIR = "/home/huaziyue/all-envs/experiment-result/test-env-1"
RESNAME= './_tmp_env_1.json'
PICNAME = './_tmp_env_1.png'
if __name__ == "__main__":
    if sys.argv[1]:
        TEST_ENV_DIR = get_test_env_dir_by_id(int(sys.argv[1]))
        RESNAME = f"./_tmp_env_{int(sys.argv[1])}.json"
        PICNAME = f"./_tmp_env_{int(sys.argv[1])}.png"
    one_env_main(TEST_ENV_DIR)
    blueprint(f'total: {len(GLOBAL_RES)} logs')
    json.dump(GLOBAL_RES,open(RESNAME,'w'))
    blueprint(f"save to {RESNAME}")


    # plot
    pda =pd.read_json(open(RESNAME))
    pda[ "logtime" ] = pd.to_datetime(pda[ "logtime" ])
    pda = pda.sort_values(by='logtime')
    # ll = {'mismatch':[],
    # 'crash':[],
    # 'total number reached':[],
    # 'a specific database failed':[]}
    # for idx,i in enumerate(pda['errortype']):
    #     if idx == 0:
    #         for k,v in ll.items():
    #             v.append( 1 if i == k else 0)
    #     else:
    #         for k,v in ll.items():
    #             v.append(ll[k][-1]+1 if i == k else ll[k][-1])

    # final_data = pd.DataFrame(
    #     {
    #         'logtime': pda['logtime'],
    #         'mismatch':pd.Series(ll['mismatch']),
    #         'crash':pd.Series(ll['crash']),
    #         'total number reached':pd.Series(ll['total number reached']),
    #         'a specific database failed':pd.Series(ll['a specific database failed'])
    #     }
    # )
    # sns.lineplot(data=final_data)
    # # plt.show()
    # plt.savefig(PICNAME)
    # blueprint(f"figure saved to {PICNAME}")
    blueprint(f"clustering ...... ")

    # ****************** cluster crash below ********* ********* ********* *********
    
    _ = set(list(pda['firstline_causedby']))
    if None in _: _.remove(None)
    if 'Timeout' in _: _.remove('Timeout')
    type_of_crash = list(_)

    config = drain3.template_miner_config.TemplateMinerConfig()
    tmpini = tempfile.NamedTemporaryFile("w+t")
    # strict format!
    tmpini.write(r"""[SNAPSHOT]
snapshot_interval_minutes = 10
compress_state = True

[MASKING]
masking = [
          {"regex_pattern":"((?<=[^A-Za-z0-9])|^)(([0-9a-f]{2,}:){3,}([0-9a-f]{2,}))((?=[^A-Za-z0-9])|$)", "mask_with": "ID"},
          {"regex_pattern":"((?<=[^A-Za-z0-9])|^)(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})((?=[^A-Za-z0-9])|$)", "mask_with": "IP"},
          {"regex_pattern":"((?<=[^A-Za-z0-9])|^)([0-9a-f]{6,} ?){3,}((?=[^A-Za-z0-9])|$)", "mask_with": "SEQ"},
          {"regex_pattern":"((?<=[^A-Za-z0-9])|^)([0-9A-F]{4} ?){4,}((?=[^A-Za-z0-9])|$)", "mask_with": "SEQ"},
          {"regex_pattern":"((?<=[^A-Za-z0-9])|^)(0x[a-f0-9A-F]+)((?=[^A-Za-z0-9])|$)", "mask_with": "HEX"},
          {"regex_pattern":"((?<=[^A-Za-z0-9])|^)([\\-\\+]?\\d+)((?=[^A-Za-z0-9])|$)", "mask_with": "NUM"},
          {"regex_pattern":"(?<=executed cmd )(\".+?\")", "mask_with": "CMD"}
          ]
mask_prefix = <:
mask_suffix = :>

[DRAIN]
sim_th = 0.4
depth = 4
max_children = 100
max_clusters = 1024
extra_delimiters = ["_"]

[PROFILING]
enabled = True
report_sec = 30""")
    tmpini.seek(0)
    config.load(tmpini.name)
    miner = drain3.TemplateMiner(None,config)
    for log_line in type_of_crash:
        result = miner.add_log_message(log_line)
        result_json = json.dumps(result)
        # print(result_json)
        template = result["template_mined"]
        params = miner.extract_parameters(template, log_line)
        # print("Parameters: " + str(params))
    for cluster in miner.drain.clusters:
        blueprint(cluster)
    
    # infer    
    result= {"not found":[]}
    for log_line in pda['firstline_causedby']:
        if log_line == None or log_line == 'Timeout':
            continue
        else:
            cluster = miner.match(log_line)
            if cluster is None:
                # print(f"No match found")
                result['not found'].append(log_line)
            else:
                template = cluster.get_template()
                if cluster.cluster_id not in result.keys():
                    result[cluster.cluster_id+1 = [log_line]
                else:
                    result[cluster.cluster_id].append(log_line)
                # print(f"Matched template #{cluster.cluster_id}: {template}")    
    res = {k:len(v) for k,v in result.items()}
    blueprint(f"{res}")