package systeminfo;

public class PciFunction {
    public int functionIndex;
    public String vendorId;
    public String productId;    

    public PciFunction (int functionIndex, String vendorId, String productId) {
        this.functionIndex = functionIndex;
        this.vendorId = vendorId;
        this.productId = productId;
    }
}