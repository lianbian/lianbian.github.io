hexo clean
hexo g
curl -H 'Content-Type:text/plain' --data-binary @public/urls.txt "http://data.zz.baidu.com/urls?site=https://www.lianbian.net&token=qrt0ct8hhTWG9pnP"
git add .
git commit -m "博客文章发布"
git push origin

# 为生成页面创建单独分支
cd ./public
git remote add deploy root@139.224.69.35:/root/www.lianbian.net.git
git branch lianbian-pages
git checkout lianbian-pages
git add .
git commit -m "Builder at $(date +'%Y-%m-%d %H:%M:%S')"
git push deploy

# 推送主仓库
cd ..
git push deploy
