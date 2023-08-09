# 在stages标签下可以定义多个执行阶段，名称可以随意定义，在git仓库发生任何变化时，都会按照这里定义的顺序来执行对应的script。
stages:
- check
- build
- publish

# 这是其中一个job，名称随意
check-simple:
# 该job所属的stage：上面stages中定义的某一项
stage: check
# 该job对应的执行过滤器，分支或tag名满足这里定义的规则才会执行script，这里是当master上有代码push或合并时执行
only:
refs:
- master
  except:
- branches
# job执行的脚本，根据我们安装runner时指定的执行器类型，这里实际是shell命令，或者是shell脚本文件
script:
- echo "empty script - check!"

# 在创建DEV或PROD开头的tag时执行
build-all:
stage: build
only:
- /^DEV_.*$/
- /^PROD_.*$/
# 分支上提交代码时不执行，仅在打tag时执行（仅供参考）
except:
- branches
  script:
# 真正的安卓编译开始，建议gradle添加环境变量，或者写全路径: /home/env/gradle-6.6.1/bin/gradle clean ....
# 现在实际上是在项目根目录，执行打包需要执行的是app下的gradle脚本
- gradle clean assembleRelease -b ./app/build.gradle
# 打包成功后，把apk文件拷贝到指定目录
- cp ./app/build/outputs/release/app-release.apk /home/apks/simple-name.apk

# 在创建PROD开头的tag时执行
publish-simple:
stage: publish
only:
- /^PROD_.*$/
  except:
- branches
  script:
- echo "empty script - publish!"
  