hexo clean
hexo g
curl -H 'Content-Type:text/plain' --data-binary @public/urls.txt "http://data.zz.baidu.com/urls?site=https://www.lianbian.net&token=qrt0ct8hhTWG9pnP"
git add .
git commit -m "博客文章发布"
git push origin
git push deploy

# 为生成页面创建单独分支
cd ./public && git init -b lianbian-pages && git add .
git config user.name "lianbian"
git config user.email "ilianbian@163.com"
git add .
git commit -m "Builder at $(date +'%Y-%m-%d %H:%M:%S')"
git push --force --quiet "root@139.224.69.35:/root/www.lianbian.net.git" lianbian-pages:lianbian-pages

git push deploy
