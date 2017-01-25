package research.dlsu.crabapp;

import java.sql.Date;

/**
 * Created by courtneyngo on 4/26/16.
 */
public class CrabUpdate {

    private long id;
    private String path;
    private Date date;

    // additional
    private String result;

    // private long idCrab;
    private long serverIdCrabUpdate;
    // private long serverIdCrab;

    private int adapterPosition;

    private CrabType crabType;
    public enum CrabType{
        SCYLLA_SERRATA, SCYLlA_TRANQUEBARICA
    }

    public CrabUpdate(){}

    public CrabUpdate(long id, String path, Date date) {
        this.id = id;
        this.path = path;
        this.date = date;
    }

    public CrabUpdate(long id, String path, Date date, CrabType crabType) {
        this.id = id;
        this.path = path;
        this.date = date;
        this.crabType = crabType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

//    public long getIdCrab() {
//        return idCrab;
//    }
//
//    public void setIdCrab(long idCrab) {
//        this.idCrab = idCrab;
//    }
//
    public long getServerIdCrabUpdate() {
        return serverIdCrabUpdate;
    }

    public void setServerIdCrabUpdate(long serverIdCrabUpdate) {
        this.serverIdCrabUpdate = serverIdCrabUpdate;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    //
//    public long getServerIdCrab() {
//        return serverIdCrab;
//    }
//
//    public void setServerIdCrab(long serverIdCrab) {
//        this.serverIdCrab = serverIdCrab;
//    }

    public int getAdapterPosition() {
        return adapterPosition;
    }

    public void setAdapterPosition(int adapterPosition) {
        this.adapterPosition = adapterPosition;
    }

    public CrabType getCrabType() {
        return crabType;
    }

    public void setCrabType(CrabType crabType) {
        this.crabType = crabType;
    }
}
