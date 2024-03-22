import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StateObject {

     private String state;
     private String localDateTime;

     public StateObject(String state) {
          this.state = state;
          this.localDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
     }

     public void setLocalDateTime() {
          this.localDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
     }

     public String getLocalDateTime() {
          return localDateTime;
     }

     public String getState() {
          return state;
     }
}
