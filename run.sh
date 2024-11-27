
kill -9 $(ps aux | grep "java -jar build/libs/hhRecruiter-0.0.1.jar" | grep -v grep | awk '{print $2}')

nohup java -jar build/libs/hhRecruiter-0.0.1.jar > build/resources/loger.txt 2>&1 &