package hust.oop.thuvienlichsu.scraper;

import hust.oop.thuvienlichsu.entity.NhanVat;
import hust.oop.thuvienlichsu.utils.StringFormater;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NhanVatScraper {
    private final String URL = "https://vansu.vn/viet-nam/viet-nam-nhan-vat?page=";
    private final String ROOT = "https://vansu.vn";

    private final int START_PAGE = 36;
    private final int END_PAGE = 36;
    private List<NhanVat> danhSachNhanVat;

    public List<NhanVat> getDanhSachNhanVat() {
        return danhSachNhanVat;
    }

    public NhanVatScraper() {
        this.danhSachNhanVat = new ArrayList<>();
        scrap();
        System.out.println("Scraping for Nhan Vat is done !");
    }

    private void scrapInSubUrl(String subUrl) {
        try {
            NhanVat nhanVat = new NhanVat();
            Document doc = Jsoup.connect(subUrl).get();

            List<String> hoTen = new ArrayList<>();
            nhanVat.addHoTen(doc.select(".breadcrumb .active").text());
            nhanVat.setNamSinh(StringFormater.findNumbersInString("..."));
            nhanVat.setNamMat(StringFormater.findNumbersInString("..."));

            Elements result = doc.select("table.table tbody tr");
            for(int i = 0; i < result.size()-1; i++) {
                Elements tmp = result.get(i).select("td");
                if(tmp.get(0).text().equals("Tên khác")) {
                    String tenKhac = tmp.get(1).text();
                    for(String ten : StringFormater.specForTenKhacNhanVat(tenKhac, nhanVat.getHoTen().get(0))) {
                        nhanVat.addHoTen(ten);
                    }
                }
                if(tmp.get(0).text().equals("Tỉnh thành")) nhanVat.setQueQuan(tmp.get(1).text());
                if(tmp.get(0).text().equals("Thời kì")) {
                    String nhomThoiKi = tmp.get(1).text();
                    nhanVat.setTenThoiKi(StringFormater.specForTenThoiKiString(nhomThoiKi));
                }
                if(tmp.get(0).text().equals("Năm sinh")) {
                    String ns = tmp.get(1).text();
                    List<String> ans = StringFormater.removeMinusAtMiddle(ns, "");
                    nhanVat.setNamSinh(StringFormater.findNumbersInString(ans.get(0)));
                    nhanVat.setNamMat(StringFormater.findNumbersInString(ans.get(1)));
                }
            }
            this.danhSachNhanVat.add(nhanVat);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void scrap() {
        for(int i = START_PAGE; i <= END_PAGE; i++) {
            String page = URL + String.valueOf(i);
            try {
                Document doc = Jsoup.connect(page).get();
                Elements result = doc.select("table.table tr td a");
                for(int j = 0; j < result.size(); j += 2) {
                    scrapInSubUrl(ROOT + result.get(j).attr("href"));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
