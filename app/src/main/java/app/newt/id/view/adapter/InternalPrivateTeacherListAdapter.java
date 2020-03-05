package app.newt.id.view.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.net.URLEncoder;
import java.util.List;

import app.newt.id.R;
import app.newt.id.helper.Constant;
import app.newt.id.helper.Utils;
import app.newt.id.server.model.PrivateTeacher;
import app.newt.id.view.custom.MathView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Erick Sumargo on 8/31/2016.
 */
public class InternalPrivateTeacherListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;

    private RecyclerView.ViewHolder mainHolder;
    private View itemView;

    private List<PrivateTeacher> teachers;

    public InternalPrivateTeacherListAdapter(List<PrivateTeacher> teachers) {
        this.teachers = teachers;
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        private CardView item;
        private CircleImageView photo;
        private TextView name, age, tuition, experience, education, lesson;
        private MathView address;
        private LinearLayout tuitionCont;

        public ItemViewHolder(View view) {
            super(view);

            context = view.getContext();
            item = view.findViewById(R.id.item);
            photo = view.findViewById(R.id.photo);
            name = view.findViewById(R.id.name);
            age = view.findViewById(R.id.age);
            tuition = view.findViewById(R.id.tuition);
            address = view.findViewById(R.id.address);
            experience = view.findViewById(R.id.experience);
            education = view.findViewById(R.id.education);
            lesson = view.findViewById(R.id.lesson);

            tuitionCont = view.findViewById(R.id.tuition_container);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_internal_private_teacher, parent, false);
        mainHolder = new ItemViewHolder(itemView);

        return mainHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final PrivateTeacher teacher = teachers.get(position);

        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        Picasso.with(context).load(Utils.with(context).getURLMediaImage(teacher.getPhoto(), "private_teacher"))
                .placeholder(R.drawable.avatar)
                .fit()
                .centerCrop()
                .into(itemHolder.photo);

        itemHolder.name.setText(teacher.getName());
        itemHolder.age.setText(teacher.getAge() + " thn");

        if (teacher.getTuition() != null) {
            itemHolder.tuition.setText(teacher.getTuition());
            itemHolder.tuitionCont.setVisibility(View.VISIBLE);
        } else {
            itemHolder.tuition.setText("-");
            itemHolder.tuitionCont.setVisibility(View.GONE);
        }
        itemHolder.address.setDisplayText(teacher.getAddress());
        itemHolder.experience.setText(teacher.getExperience() + " thn");

        String[] educationParts = teacher.getEducation().split("%");
        String educations = "";
        for (int i = 0; i < educationParts.length; i++) {
            educations += "- " + educationParts[i];
            if (i < educationParts.length - 1) {
                educations += "\n";
            }
        }
        itemHolder.education.setText(educations);

        String[] lessonParts = teacher.getLesson().split("%");
        String lessons = "";
        for (int i = 0; i < lessonParts.length; i++) {
            lessons += "- " + lessonParts[i];
            if (i < lessonParts.length - 1) {
                lessons += "\n";
            }
        }
        itemHolder.lesson.setText(lessons);

        itemHolder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Konfirmasi")
                        .setMessage("Tanya lebih lanjut tentang guru?")
                        .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                String phone = "+6285371117293";
                                try {
                                    String url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" +
                                            URLEncoder.encode("Halo Newt.\nBolehkah saya tanya lebih lanjut tentang guru bernama " + teacher.getName() + "?", "UTF-8");
                                    intent.setPackage("com.whatsapp");
                                    intent.setData(Uri.parse(url));
                                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                                        context.startActivity(intent);
                                    } else {
                                        intent = new Intent(Intent.ACTION_VIEW);
                                        try {
                                            intent.setData(Uri.parse("line://ti/p/~newt"));
                                            context.startActivity(intent);
                                        } catch (Exception e) {
                                            Toast.makeText(context, "Maaf, anda tidak memiliki aplikasi media sosial apapun untuk bertanya lebih lanjut tentang guru", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } catch (Exception e) {
                                }
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return teachers.size();
    }
}