package cn.edu.nju.ws.expedia.model.wordnet.query;

import cn.edu.nju.ws.expedia.database.DBConnectionFactory;
import cn.edu.nju.ws.expedia.model.wordnet.Synset;
import cn.edu.nju.ws.expedia.util.FourTuple;
import cn.edu.nju.ws.expedia.util.TwoTuple;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xiangqian on 2015/1/4.
 */
public class WordnetQueryAgentDBImpl implements WordnetQueryAgent {

    /**
     * return the list of words in the given synset.
     * @param synsetID
     * @return a list for FourTuple \<word_id, word, pos, lexfilenum\>.
     */
    public List<FourTuple<Integer, String, Character, Integer>> getWordsOfSynset(String synsetID){
        sysnetIDTest(synsetID);
        List<FourTuple<Integer, String, Character, Integer>> words = new
                ArrayList<FourTuple<Integer, String, Character, Integer>>();

        String sql = "SELECT words.word_id, words.word, words.pos, words.lex_id FROM words, synset_words " +
                "WHERE words.word_id=synset_words.word_id AND synset_words.synset_id=?;";

        Connection conn = DBConnectionFactory.getInstance().getDefaultDBConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try{
            ps = conn.prepareStatement(sql);
            ps.setString(1, synsetID);
            rs = ps.executeQuery();
            while(rs.next()){
                int wid = rs.getInt(1);
                String word = rs.getString(2);
                String pos = rs.getString(3);
                int lnum = rs.getInt(4);

                words.add(new FourTuple<Integer, String, Character, Integer>(wid, word, pos.charAt(0), lnum));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally{
            if(rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            if(ps != null)
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return words;
    }

    private static void sysnetIDTest(String synsetID){
        if(synsetID == null || ! Synset.isSynsetID(synsetID))
            throw new IllegalArgumentException("The argument synsetID (" + synsetID + ") is null or illegal.");
    }

    /**
     * return the pointers with the given synsetID.
     * @param synsetID
     * @param ptrType this can be null. If that, this returns all pointers. If not, this returns the pointers with the given type.
     * @return list of TwoTuple \<pointer type, target synset id\>.
     */
    public List<TwoTuple<String, String>> getPointersOfSynset(String synsetID, String ptrType){
        sysnetIDTest(synsetID);

        List<TwoTuple<String, String>> ptrs = new ArrayList<TwoTuple<String, String>>();
        String sql = "SELECT " + (ptrType == null ? "ptrtype, ": "") +"synset_tar_id FROM synset_pointers" +
                " WHERE synset_src_id=?" + (ptrType == null ? ";" : " AND ptr_type='" + ptrType + "';");

        Connection conn = DBConnectionFactory.getInstance().getDefaultDBConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try{
            ps = conn.prepareStatement(sql);
            ps.setString(1, synsetID);
            rs = ps.executeQuery();
            while(rs.next()){
                String ptype = (ptrType == null ? rs.getString("ptrType") : ptrType);
                String tarSID = rs.getString("synset_tar_id");

                ptrs.add(new TwoTuple<String, String>(ptype, tarSID));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally{
            if(rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            if(ps != null)
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return ptrs;
    }

    public void loadSynset(Synset synset) {
        String sql = "SELECT lex_file_num, num_of_words, num_of_pointers," +
                "gloss FROM synsets WHERE id=?;";
        Connection conn = DBConnectionFactory.getInstance().getDefaultDBConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try{
            ps = conn.prepareStatement(sql);
            ps.setString(1, synset.getIdentifier());
            rs = ps.executeQuery();

            if(rs.next()){
                int lfnum = rs.getInt(1);
                int numWords = rs.getInt(2);
                int numPtrs = rs.getInt(3);
                String gloss = rs.getString(4);

                synset.setCatID(lfnum);
                synset.setNumPointers(numPtrs);
                synset.setNumWords(numWords);
                synset.setGloss(gloss);
            }
            else {
                System.err.println("No result for: " + synset.getIdentifier());
            }

        }catch(SQLException e){
            e.printStackTrace();
        }finally{
            if(rs != null)
                try {
                    rs.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            if(ps != null)
                try{
                    ps.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            try{
                conn.close();
            }catch(SQLException e){
                e.printStackTrace();
            }
        }
    }

    public Synset getSynset(String sid) {
        String sql = "SELECT lex_file_num, num_of_words, num_of_pointers," +
                "gloss FROM synsets WHERE id=?;";
        Connection conn = DBConnectionFactory.getInstance().getDefaultDBConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        Synset synset = null;
        try{
            ps = conn.prepareStatement(sql);
            ps.setString(1, sid);
            rs = ps.executeQuery();

            if(rs.next()){
                int lfnum = rs.getInt(1);
                int numWords = rs.getInt(2);
                int numPtrs = rs.getInt(3);
                String gloss = rs.getString(4);

                synset = new Synset(sid, sid.charAt(0), lfnum, numWords, numPtrs, gloss);

            }
            else {
                System.err.println("No result for: " + sid);
            }

        }catch(SQLException e){
            e.printStackTrace();
        }finally{
            if(rs != null)
                try {
                    rs.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            if(ps != null)
                try{
                    ps.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            try{
                conn.close();
            }catch(SQLException e){
                e.printStackTrace();
            }
        }

        return synset;
    }
}
