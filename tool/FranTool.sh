if[ -f $2 ];
   cd ${2%/*}

exec java -jar  $(cd $(dirname ${BASH_SOURCE:-$0});pwd)/FranTool.jar $@

