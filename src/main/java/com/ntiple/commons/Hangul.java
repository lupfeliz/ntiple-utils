/**
 * @File        : Hangul.java
 * @Version     : $Rev$
 * @Author      : 정재백
 * @History     : 2023-11-26 최초 작성
 * @Description : 한글유틸
 **/
package com.ntiple.commons;

// import lombok.extern.slf4j.Slf4j;

// @Slf4j
public class Hangul {

  public static final int BASE_CODE = 0xAC00;
  public static final int BLANK = '　';

  public static final char[][] TB_CJJ = new char[][] {
    /** 19개 초성 자음 */
    { 'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ' },
    /** 21개 중성 모음 */
    { 'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ' },
    /** 16개 종성 자음 (27칸) */
    { '　', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅌ', 'ㅍ', 'ㅎ' },
  };

  /** 세벌식(최종) 키보드 스트로크 테이블 */
  public static final char[][][] TB_3BULF = new char[][][] {
    /** 19개 초성 자음 */
    { {'k'}, {'k', 'k'}, {'h'}, {'u'}, {'u', 'u'}, {'y'}, {'i'}, {';'}, {';', ';'}, {'n'}, {'n', 'n'}, {'j'}, {'l'}, {'l', 'l'}, {'o'}, {'0'}, {'\''}, {'p'}, {'m'} },
    /** 21개 중성 모음 */
    { {'f'}, {'r'}, {'6'}, {'G'}, {'t'}, {'c'}, {'e'}, {'7'}, {'v'}, {'v', 'f'}, {'v', 'r'}, {'v', 'd'}, {'4'}, {'b'}, {'b', 't'}, {'b', 'c'}, {'b', 'd'}, {'5'}, {'g'}, {'g' ,'d'}, {'d'} },
    /** 16개 종성 자음 (27칸) */
    { {'　'}, {'x'}, {'x', 'x'}, {'V'}, {'s'}, {'E'}, {'S'}, {'A'}, {'w'}, {'@'}, {'F'}, {'D'}, {'T'}, {'%'}, {'$'}, {'R'}, {'z'}, {'3'}, {'X'}, {'q'}, {'q', 'q'}, {'a'}, {'#'}, {'Z'}, {'W'}, {'Q'}, {'1'} },
  };

  /** 두벌식 키보드 스트로크 테이블 */
  public static final char[][][] TB_2BUL = new char[][][] {
    /** 19개 초성 자음 */
    { {'r'}, {'R'}, {'s'}, {'e'}, {'E'}, {'f'}, {'a'}, {'q'}, {'Q'}, {'t'}, {'T'}, {'d'}, {'w'}, {'W'}, {'c'}, {'z'}, {'x'}, {'v'}, {'g'} },
    /** 21개 중성 모음 */
    { {'k'}, {'o'}, {'i'}, {'O'}, {'j'}, {'p'}, {'u'}, {'P'}, {'h'}, {'h', 'k'}, {'h', 'o'}, {'h', 'l'}, {'y'}, {'n'}, {'n', 'j'}, {'n', 'p'}, {'n', 'l'}, {'b'}, {'m'}, {'m', 'l'}, {'l'} },
    /** 16개 종성 자음 (27칸) */
    { {'　'}, {'r'}, {'R'}, {'r', 't'}, {'s'}, {'s', 'w'}, {'s', 'g'}, {'e'}, {'f'}, {'f', 'r'}, {'f', 'a'}, {'f', 'q'}, {'f', 't'}, {'f', 'x'}, {'f', 'v'}, {'f', 'g'}, {'a'}, {'q'}, {'q', 't'}, {'t'}, {'T'}, {'d'}, {'w'}, {'c'}, {'x'}, {'v'}, {'g'} },
  };

  public static final char[][] LST_JOSA = new char[][] {
    {'을', '를'},
    {'은', '는'},
    {'이', '가'},
    {'의', '의'},
    {'에', '에'},
  };

  public static char[] extract(char ch) {
    char[] ret = new char[] { ' ', ' ', ' ' };
    int[] ext = extractInx(ch);
    ret[0] = TB_CJJ[0][ext[0]];
    ret[1] = TB_CJJ[1][ext[1]];
    ret[2] = TB_CJJ[2][ext[2]];
    return ret;
  }

  /** 1글자의 초, 중, 종성을 분리한다. */
  public static int[] extractInx(char ch) {
    int[] ret = new int[] { 0, 0, 0 };
    int code = ch;
    int mod = code - BASE_CODE;
    /** 초성분리 */
    for (int inx = TB_CJJ[0].length - 1; inx >= 0; inx--) {
      if (mod >= (inx * 588)) {
        mod = mod - inx * 588;
        // ret[0] = TB_CJJ[0][inx];
        ret[0] = inx;
        break;
      }
    }
    // log.trace("MOD-1:{}, {}", mod, ret);
    /** 중성분리 */
    for (int inx = TB_CJJ[1].length - 1; inx >= 0; inx--) {
      if (mod >= (inx * 28)) {
        mod = mod - inx * 28;
        // ret[1] = TB_CJJ[1][inx];
        ret[1] = inx;
        break;
      }
    }
    // log.trace("MOD-2:{}, {}", mod, ret);
    /** 종성분리 (사실 종성 자체는 이 시점에서 바로 mod 값임, 로직 정리를 위해 아래 코드 기재) */
    for (int inx = TB_CJJ[2].length - 1; inx >= 0; inx--) {
      if (TB_CJJ[2][inx] == BLANK) { continue; }
      if (mod >= inx) {
        mod = mod - inx;
        // ret[2] = TB_CJJ[2][inx];
        ret[2] = inx;
        break;
      }
    }
    // log.trace("MOD-3:{}, {}", mod, ret);
    /** mod 는 여기서 0 이어야 한다. */
    return ret;
  }

  public static char[] extractkbst(String str, char[][][] TB) {
    char[] ret = null;
    if (str == null) { return ret; }
    char[][] res = new char[str.length()][];
    int nlen = 0;
    for (int inx = 0; inx < res.length; inx++) {
      res[inx] = extractkbst(str.charAt(inx), TB);
      nlen += res[inx].length;
    }
    ret = new char[nlen];
    nlen = 0;
    for (int inx = 0; inx < res.length; inx++) {
      for (int inx2 = 0; inx2 < res[inx].length; inx2++, nlen++) {
        ret[nlen] = res[inx][inx2];
      }
    }
    return ret;
  }

  public static char[] extractkbst(char ch, char[][][] TB) {
    char[] ret = null;
    int[] ext = extractInx(ch);
    char[][] res = new char[3][];
    int nlen = 0;
    for (int inx = 0; inx < res.length; inx++) {
      res[inx] = TB[inx][ext[inx]];
      if ((res[inx].length == 1 && res[inx][0] == BLANK)) { continue; }
      nlen ++;
    }
    ret = new char[nlen];
    nlen = 0;
    for (int inx = 0; inx < res.length; inx++) {
      if ((res[inx].length == 1 && res[inx][0] == BLANK)) { continue; }
      for (int inx2 = 0; inx2 < res[inx].length; inx2++, nlen++) {
        ret[nlen] = res[inx][inx2];
      }
    }
    return ret;
  }
}
