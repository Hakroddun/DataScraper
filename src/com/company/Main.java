package com.company;

import GUI.Components.CircularLoadingBar;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;

public class Main
{
    int PageNumber = 1;
    int FCMembersParsed = 0;
    int FCMembersTotal = 0;
    public static void main(String[] args)
    {
        try
        {
            Main mainDo = new Main();
            EventQueue.invokeLater(() -> {
                JFrame f = new JFrame();
                f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                f.getContentPane().add(mainDo.CreateLoadingBarUI());
                f.setSize(320, 240);
                f.setLocationRelativeTo(null);
                f.setVisible(true);
            });
            mainDo.ScrapeFreecompanyForData("9229283011365761186");
            System.exit(-1);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public JComponent CreateLoadingBarUI() {
        JProgressBar progress = new JProgressBar();
        // use JProgressBar#setUI(...) method
        progress.setUI(new CircularLoadingBar());
        progress.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        progress.setStringPainted(true);
        progress.setFont(progress.getFont().deriveFont(24f));
        progress.setForeground(Color.ORANGE);

        (new Timer(50, e -> {
            if (FCMembersParsed >0)
            {
                int membersParsed = (int) Math.round(FCMembersParsed* 100 / FCMembersTotal);
                int memberProgress = Math.min(100, membersParsed);
                progress.setValue(memberProgress);
            }
        })).start();

        JPanel p = new JPanel();
        p.add(progress);
        return p;
    }

    private void ScrapeFreecompanyForData(String FreeCompanyID) throws IOException
    {
        FCMembersTotal = GetFCMembersTotal(FreeCompanyID);

        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter("MemberData.csv", "UTF-8");
            while (FCMembersParsed != FCMembersTotal)
            {
                Elements MemberElements = getFCMemberElements(FreeCompanyID);
                PrintMembers(MemberElements,writer);
                PageNumber++;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            writer.close();
        }
    }
    private int GetFCMembersTotal(String FreeCompanyID) throws IOException
    {
        org.jsoup.nodes.Document doc = Jsoup.connect("https://na.finalfantasyxiv.com/lodestone/freecompany/"+FreeCompanyID+"/member").get();
        Element bodyDoc = doc.select("div.parts__total").first();
        String line = bodyDoc.toString();
        int TotalMembers = Integer.parseInt(line.substring(26,line.indexOf("Total")).trim());
        System.out.println("FC members total: "+TotalMembers);
        return TotalMembers;
    }

    private Elements getFCMemberElements(String FreeCompanyID) throws IOException
    {
        org.jsoup.nodes.Document doc = Jsoup.connect("https://na.finalfantasyxiv.com/lodestone/freecompany/"+FreeCompanyID+"/member/?page="+PageNumber).get();
        return doc.select("a[href].entry__bg");
    }

    private void PrintMembers(Elements MemberElements, PrintWriter writer) throws IOException
    {
        for (int i = 0; i < MemberElements.size(); i++)
        {
            String MemberDetails = "";
            String MemberID = MemberElements.get(i).getElementsByClass("entry__bg").attr("href").replaceAll("[^0-9]", "");
            String MemberName = MemberElements.get(i).getElementsByClass("entry__name").text();
            System.out.println("ID: " + MemberID);
            System.out.println("Name: " + MemberName);
            MemberDetails = MemberName;
            MemberDetails = MemberDetails.concat(ScrapeMemberForData(MemberID));
            System.out.println(MemberDetails);
            writer.println(MemberDetails);
            FCMembersParsed++;
            System.out.println("\n");
        }
    }


    private String ScrapeMemberForData(String MemberID) throws IOException
    {
            Elements MemberElements = getMemberElement(MemberID);
            return PrintMemberDetails(MemberElements);
    }

    private Elements getMemberElement(String MemberID) throws IOException
    {
        org.jsoup.nodes.Document doc = Jsoup.connect("https://na.finalfantasyxiv.com/lodestone/character/"+MemberID+"/").get();
        return doc.select("div.character__job__level, div.character__job__name");
    }

    private String PrintMemberDetails(Elements MemberElements)
    {
        String JobLevels = "";

        for (int i = 0;i<MemberElements.size();i++)
        {
            String line = MemberElements.get(i).toString();
            String MemberJobLevel = MemberElements.get(i).getElementsByClass("character__job__level").text();
            i++;
            String MemberJobName = MemberElements.get(i).getElementsByClass("character__job__name").text();
            //System.out.println(MemberElements.get(i));
            System.out.println("JobName: "+MemberJobName);
            System.out.println("JobLevel: "+MemberJobLevel);
            JobLevels = JobLevels+","+MemberJobLevel;
        }
        return JobLevels;
    }
}
