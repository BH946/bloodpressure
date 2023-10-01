package mica.part1.checkMate.Home.BloodPressure;

import android.util.Log;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelSheetHandlerjob implements XSSFSheetXMLHandler.SheetContentsHandler {

    private int currentCol = -1;
    private int currRowNum = 0;
    String filePath = "";
    private List<List<String>> rows = new ArrayList<List<String>>();    //실제 엑셀을 파싱해서 담아지는 데이터
    private List<String> row = new ArrayList<String>();
    private List<String> header = new ArrayList<String>();

    public static ExcelSheetHandlerjob readExcel(File file ) throws Exception {

        ExcelSheetHandlerjob sheetHandler = new ExcelSheetHandlerjob();
        try {
            //org.apache.poi.openxml4j.opc.OPCPackage
            OPCPackage opc = OPCPackage.open( file );
            //org.apache.poi.xssf.eventusermodel.XSSFReader
            XSSFReader xssfReader = new XSSFReader( opc );
            //org.apache.poi.xssf.model.StylesTable
            StylesTable styles = xssfReader.getStylesTable();
            //org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable( opc );
            //엑셀의 시트를 하나만 가져오기입니다.
            InputStream inputStream = xssfReader.getSheetsData().next();
            InputStream b = xssfReader.getSheet("rId3");
            //org.xml.sax.InputSource
            InputSource inputSource = new InputSource( b );
            //org.xml.sax.Contenthandler
            ContentHandler handle = new XSSFSheetXMLHandler( styles, strings, sheetHandler, false );
            XMLReader xmlReader = SAXHelper.newXMLReader();
            xmlReader.setContentHandler( handle );
            xmlReader.parse( inputSource );
            inputStream.close();
            opc.close();
        } catch ( Exception e ) {
            // 에러 발생했을때 하시고 싶은 TO-DO
            Thread.sleep(1000); // 1초 딜레이 주기(혈압 xlsx파일 찾는과정)
            return readExcel(file);
            // Log.d("123123123", "error");
        }
        return sheetHandler;
    }//readExcel - end

    public List<List<String>> getRows() {
        return rows;
    }

    @Override
    public void startRow( int arg0 ) {
        this.currentCol = -1;
        this.currRowNum = arg0;
    }

    @Override
    public void cell( String columnName, String value, XSSFComment var3 ) {
        int iCol = ( new CellReference( columnName ) ).getCol();
        int emptyCol = iCol - currentCol - 1;
        for ( int i = 0; i < emptyCol; i++ ) {
            row.add( "" );
        }
        currentCol = iCol;
        row.add( value );
    }

    @Override
    public void headerFooter( String arg0, boolean arg1, String arg2 ) {
        //사용안합니다.
    }

    @Override
    public void endRow( int rowNum ) {
        if ( rowNum == 0 ) {
            header = new ArrayList( row );
        } else {
            if ( row.size() < header.size() ) {
                for ( int i = row.size(); i < header.size(); i++ ) {
                    row.add( "" );
                }
            }
            rows.add( new ArrayList( row ) );
        }
        row.clear();
    }

    public void hyperlinkCell( String arg0, String arg1, String arg2, String arg3, XSSFComment arg4 ) {
        // TODO Auto-generated method stub
    }
}
