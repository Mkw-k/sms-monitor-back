package com.mk.www.smsmonitor.transaction.infrastructure.exporter;


//TODO 나중에 디비에서 불러오는거로 변경
//구글시트 관련 상수
@Deprecated
public final class GoogleSheetConstants {
    private GoogleSheetConstants() {}

    // 시트 & 환경설정
    public static final String SPREADSHEET_ID = "1g0P2zNo6MwpYDxXs8RMeCAztAKbyWMkYxuRIdIDX1OI";
    public static final String APPLICATION_NAME = "SpringSheetApp";
    public static final String GOOGLE_SHEET_API_URL = "https://www.googleapis.com/auth/spreadsheets";
    public static final String GOOGLE_SHEET_CREDENTIALS_FILE_PATH = "src/main/resources/google_sheet_credentials.json";

    // 시트내부 정보
    public static final String SHEET_RANGE = "sheet1!A2:I";
    public static final String STATE_CELL_ALPHA = "C";
    public static final String STATE_SHEET_NAME = "sheet1!";
    public static final String PAY_COMPLETE_STATE = "입금완료";
    public static final String VALUE_INPUT_OPTION = "RAW";

    // 스프레드시트 데이터 인덱스 및 오프셋 관련 상수
    public static final int MIN_REQUIRED_COLUMN_SIZE = 8; // 최소 8개 컬럼 (번호~금액까지)
    public static final int CUSTOMER_PHONE_INDEX = 3;     // 구매자연락처 (D열, 인덱스 3)
    public static final int PRICE_INDEX = 7;              // 금액 (H열, 인덱스 7)
    public static final int DATA_START_ROW_OFFSET = 2;    // 데이터 시작 행 오프셋 (A2 시작이므로, 2행이 첫 번째 데이터)
}
