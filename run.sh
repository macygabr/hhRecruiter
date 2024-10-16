docker stop hhRecruiter
docker rm hhRecruiter
docker rmi hhRecruiter
docker build -t hhRecruiter .
docker run -d --name hhRecruiter -p 2000:2000 hhRecruiter
docker logs -f hhRecruiter