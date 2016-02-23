require 'open-uri'
require 'nokogiri'
@doc = Nokogiri::HTML(open("http://travel.state.gov/_res/rss/TWs.xml/"))
warnings = @doc.xpath("//item")

warnings.each do |w|
  data =  { 
    title: w.xpath("title")[0].content,
    date: w.xpath("pubdate")[0].content,
    link: w.xpath("link")[0].content,
    country: w.xpath("identifier")[0].content,
    description: w.xpath("description")[0].content,
  }

  puts "#{data[:title]} for #{data[:date]}"
end
