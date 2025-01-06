hexo clean
hexo generate --watch
curl -H 'Content-Type:text/plain' --data-binary @public/urls.txt "http://data.zz.baidu.com/urls?site=https://www.lianbian.net&token=qrt0ct8hhTWG9pnP"
git add .
git commit -m "博客文章发布"
git push origin
git push deploy
