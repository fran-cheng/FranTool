
if[ -f $1 ];
    outPut=${1/.apk/.jar}
exec  $(cd $(dirname ${BASH_SOURCE:-$0});pwd)/dex-tools/d2j-dex2jar.sh -f -o $outPut $1

